<?php
// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2020 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------

namespace app\services\kefu;


use app\services\BaseServices;
use app\dao\message\service\StoreServiceDao;
use app\services\message\service\StoreServiceAuxiliaryServices;
use app\services\message\service\StoreServiceServices;
use app\services\user\UserServices;
use app\services\wechat\WechatUserServices;
use app\webscoket\SocketPush;
use think\annotation\Inject;
use think\exception\ValidateException;
use app\services\message\service\StoreServiceLogServices;
use app\services\message\service\StoreServiceRecordServices;
use think\facade\Log;

/**
 * Class KefuServices
 * @package app\services\kefu
 * @mixin StoreServiceDao
 */
class KefuServices extends BaseServices
{
    const GECJUROK = 'gElnUk';

    /**
     * @var StoreServiceDao
     */
    #[Inject]
    protected StoreServiceDao $dao;

    /**
     * 获取客服列表
     * @param array $where
     * @param array $noId
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getServiceList(array $where, array $noId)
    {
        $where['status'] = 1;
        $where['account_status'] = 1;
        $where['noId'] = $noId;
        $where['online'] = 1;
        [$page, $limit] = $this->getPageValue();
        $list = $this->dao->getServiceList($where, $page, $limit);
        $count = $this->dao->count($where);
        return compact('list', 'count');
    }

    /**
     * 获取聊天记录
     * @param int $uid
     * @param int $toUid
     * @param int $upperId
     * @param int $is_tourist
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getChatList(int $uid, int $toUid, int $upperId, int $is_tourist = 0)
    {
        /** @var StoreServiceLogServices $service */
        $service = app()->make(StoreServiceLogServices::class);
        [$page, $limit] = $this->getPageValue();
        return array_reverse($service->tidyChat($service->getServiceChatList(['chat' => [$uid, $toUid], 'is_tourist' => $is_tourist], $limit, $upperId)));
    }

    /**
     * 转移客服
     * @param int $kfuUid
     * @param int $uid
     * @param int $toUid
     * @return mixed
     */
    public function setTransfer(int $kfuUid, int $uid, int $kfuToUid)
    {
        if ($uid === $kfuToUid) {
            throw new ValidateException('自己不能转接给自己');
        }
        /** @var StoreServiceAuxiliaryServices $auxiliaryServices */
        $auxiliaryServices = app()->make(StoreServiceAuxiliaryServices::class);
        /** @var StoreServiceLogServices $service */
        $service = app()->make(StoreServiceLogServices::class);
        $addTime = $auxiliaryServices->value(['binding_id' => $kfuUid, 'relation_id' => $uid], 'update_time');
        $list = $service->getMessageList(['chat' => [$kfuUid, $uid], 'add_time' => $addTime]);
        $data = [];
        foreach ($list as $item) {
            if ($item['to_uid'] == $kfuUid) {
                $item['to_uid'] = $kfuToUid;
            }
            if ($item['uid'] == $kfuUid) {
                $item['uid'] = $kfuToUid;
            }
            $item['add_time'] = time();
            unset($item['id']);
            $data[] = $item;
        }
        $record = $this->transaction(function () use ($data, $service, $kfuUid, $uid, $kfuToUid, $auxiliaryServices) {
            if ($data) {
                $num = count($data) - 1;
                $messageData = $data[$num] ?? [];
                $res = $service->saveAll($data);
            } else {
                $num = 0;
                $res = true;
                $messageData = [];
            }
            /** @var StoreServiceRecordServices $serviceRecord */
            $serviceRecord = app()->make(StoreServiceRecordServices::class);
            $info = $serviceRecord->get(['user_id' => $kfuUid, 'to_uid' => $uid], ['type', 'message_type', 'is_tourist', 'avatar', 'nickname']);
            $record = $serviceRecord->saveRecord($uid, $kfuToUid, $messageData['msn'] ?? '', $info['type'] ?? 1, $messageData['message_type'] ?? 1, $num, $info['is_tourist'] ?? 0, $info['nickname'] ?? "", $info['avatar'] ?? '');
            $res = $res && $auxiliaryServices->saveAuxliary(['binding_id' => $kfuUid, 'relation_id' => $uid]);
            if (!$res && !$record) {
                throw new ValidateException('转接客服失败');
            }
            return $record;
        });
        try {
            if (!$record['is_tourist']) {
                /** @var UserServices $userService */
                $userService = app()->make(UserServices::class);
                $_userInfo = $userService->getUserInfo($uid, 'nickname,avatar');
                $record['nickname'] = $_userInfo['nickname'];
                $record['avatar'] = $_userInfo['avatar'];
            }
            $keufInfo = $this->dao->get(['uid' => $kfuUid], ['avatar', 'nickname']);
            if ($keufInfo) {
                $keufInfo = $keufInfo->toArray();
            } else {
                $keufInfo = (object)[];
            }
            //给转接的客服发送消息通知
            SocketPush::kefu()->type('transfer')->to($kfuToUid)->data(['recored' => $record, 'kefuInfo' => $keufInfo])->push();
            //告知用户对接此用户聊天
            $keufToInfo = $this->dao->get(['uid' => $kfuToUid], ['avatar', 'nickname']);
            SocketPush::user()->type('to_transfer')->to($uid)->data(['toUid' => $kfuToUid, 'avatar' => $keufToInfo['avatar'] ?? '', 'nickname' => $keufToInfo['nickname'] ?? ''])->push();
        } catch (\Exception $e) {
        }
        return true;
    }

    /**
     * 关键字回复，没有默认关键词会自动发送给客服
     * @param string $reply
     * @param string $openId
     * @return bool
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function replyTransferService(string $reply, string $openId)
    {
        /** @var WechatUserServices $userServices */
        $userServices = app()->make(WechatUserServices::class);
        $userInfo = $userServices->get(['openid' => $openId], ['uid', 'nickname', 'headimgurl as avatar']);
        if (!$userInfo) {
            return true;
        }
        /** @var StoreServiceServices $kfServices */
        $kfServices = app()->make(StoreServiceServices::class);
        $serviceInfoList = $kfServices->getServiceList(['status' => 1, 'online' => 1]);
        if (!count($serviceInfoList)) {
            return true;
        }
        $uids = array_column($serviceInfoList['list'], 'uid');
        if (!$uids) {
            return true;
        }
        /** @var StoreServiceRecordServices $recordServices */
        $recordServices = app()->make(StoreServiceRecordServices::class);
        //上次聊天客服优先对话
        $toUid = $recordServices->getLatelyMsgUid(['to_uid' => $userInfo['uid']], 'user_id');
        //如果上次聊天的客不在当前客服中从新获取新的客服人员
        if (!in_array($toUid, $uids)) {
            $toUid = 0;
        }
        if (!$toUid) {
            mt_srand();
            $toUid = $uids[array_rand($uids)] ?? 0;
        }
        if (!$toUid) {
            return true;
        }
        /** @var StoreServiceLogServices $logServices */
        $logServices = app()->make(StoreServiceLogServices::class);
        $num = $logServices->getMessageNum(['uid' => $userInfo['uid'], 'to_uid' => $toUid, 'type' => 0, 'is_tourist' => 0]);
        $record = $recordServices->saveRecord($userInfo['uid'], $toUid, $reply, 1, 1, $num, 0, $userInfo['nickname'] ?? "", $userInfo['avatar'] ?? '');

        $data = [
            'add_time' => time(),
            'is_tourist' => 0,
            'to_uid' => $toUid,
            'msn' => $reply,
            'uid' => $userInfo['uid'],
            'type' => 0
        ];
        $data = $logServices->save($data);
        $data = $data->toArray();
        $data['_add_time'] = $data['add_time'];
        $data['add_time'] = strtotime($data['add_time']);
        $data['record'] = $record;

        try {
            SocketPush::kefu()->type('mssage_num')->to($toUid)->data([
                'uid' => $userInfo['uid'],
                'num' => $num,
                'recored' => $data['record']
            ])->push();
            SocketPush::admin()->to($toUid)->data($data)->type('reply')->push();
        } catch (\Throwable $e) {
            Log::error('没有开启长连接无法推送消息，消息内容为：' . $reply);
        }
    }
}
