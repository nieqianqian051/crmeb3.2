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
namespace app\controller\api\v1\user;

use app\Request;
use app\services\user\member\MemberCardServices;
use app\services\user\UserServices;
use app\services\user\UserSignServices;
use think\annotation\Inject;

/**
 * 用户签到
 * Class UserSign
 * @package app\api\controller\v1\user
 */
class UserSign
{
    /**
     * @var UserSignServices
     */
    #[Inject]
    protected UserSignServices $services;


    /**
     * 签到 配置
     * @param Request $request
     * @param UserServices $userServices
     * @return mixed
     * @throws \throwable
     */
    public function sign_config(Request $request, UserServices $userServices)
    {
        $uid = (int)$request->uid();
        $signConfig = $this->services->signConfig($uid);
        //是否是付费会员
        if ($userServices->checkUserIsSvip($uid)) {
            //看是否开启签到积分翻倍奖励
            /** @var MemberCardServices $memberCardService */
            $memberCardService = app()->make(MemberCardServices::class);
            $sign_rule_number = $memberCardService->isOpenMemberCardCache('sign');
            if ($sign_rule_number) {
                foreach ($signConfig['signList'] as &$value) {
                    $value['point'] = (int)$sign_rule_number * $value['point'];
                }
                $signConfig['signData']['sign_point'] = (int)$sign_rule_number * $signConfig['signData']['sign_point'];
            }
        }
        return app('json')->successful($signConfig);
    }

    /**
     * 日历数据
     * @param Request $request
     * @param UserServices $userServices
     * @return mixed
     * @throws \throwable
     */
    public function sign_calendar(Request $request)
    {
        [$time] = $request->getMore([
            ['time', '']
        ], true);
        $uid = (int)$request->uid();
        $signCalendar = $this->services->getCalendarDate($uid, $time);
        return app('json')->successful($signCalendar);
    }

    /**
     * 签到 列表
     * @param Request $request
     * @return mixed
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function sign_list(Request $request)
    {
        [$page, $limit] = $request->getMore([
            ['page', 0],
            ['limit', 0]
        ], true);
        if (!$limit) return app('json')->successful([]);
        $uid = (int)$request->uid();
        return app('json')->successful($this->services->getUserSignList($uid));
    }

    /**
     * 签到
     * @param Request $request
     * @return mixed
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function sign_integral(Request $request)
    {
        $uid = (int)$request->uid();
        $sign = $this->services->sign($uid);
        if ($sign) {
            return app('json')->successful('签到成功', $sign);
        } else {
            return app('json')->fail('签到失败');
        }
    }

    /**
     * 签到用户信息
     * @param Request $request
     * @return mixed
     */
    public function sign_user(Request $request)
    {
        [$sign, $integral, $all] = $request->postMore([
            ['sign', 0],
            ['integral', 0],
            ['all', 0],
        ], true);
        $uid = (int)$request->uid();
        return app('json')->success($this->services->signUser($uid, $sign, $integral, $all));
    }

    /**
     * 签到列表（年月）
     *
     * @param Request $request
     * @return mixed
     */
    public function sign_month(Request $request)
    {
        $uid = (int)$request->uid();
        return app('json')->successful($this->services->getSignMonthList($uid));
    }

    /**
     * 用户设置签到提醒
     * @param Request $request
     * @param $status
     * @return \think\Response
     */
    public function sign_remind(Request $request, $status)
    {
        $uid = (int)$request->uid();
        $this->services->setSignRemind($uid, $status);
        return app('json')->success('设置成功');
    }
}
