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

namespace crmeb\services;

use Alipay\EasySDK\Payment\Common\Models\AlipayTradeFastpayRefundQueryResponse;
use Alipay\EasySDK\Payment\Common\Models\AlipayTradeRefundResponse;
use Alipay\EasySDK\Payment\Wap\Models\AlipayTradeWapPayResponse;
use crmeb\utils\Hook;
use JetBrains\PhpStorm\ArrayShape;
use think\facade\Log;
use think\facade\Route as Url;
use Alipay\EasySDK\Kernel\Config;
use Alipay\EasySDK\Kernel\Factory;
use crmeb\exceptions\PayException;
use app\services\pay\PayNotifyServices;
use Alipay\EasySDK\Kernel\Util\ResponseChecker;
use AlibabaCloud\Tea\Tea;

/**
 * Class AliPayService
 * @package crmeb\services
 */
class AliPayService
{

    /**
     * 配置
     * @var array
     */
    protected array $config = [
        'appId' => '',
        'merchantPrivateKey' => '',//应用私钥
        'alipayPublicKey' => '',//支付宝公钥
        'notifyUrl' => '',//可设置异步通知接收服务地址
        'encryptKey' => '',//可设置AES密钥，调用AES加解密相关接口时需要（可选）
    ];

    /**
     * @var ResponseChecker
     */
    protected ResponseChecker $response;

    /**
     * 付款码
     * @var string
     */
    protected string $authCode;

    /**
     * @var static
     */
    protected static $instance;

    /**
     * AliPayService constructor.
     * @param array $config
     */
    protected function __construct(array $config = [])
    {
        if (!$config) {
            $alipay = SystemConfigService::more(['ali_pay_appid', 'alipay_merchant_private_key', 'alipay_public_key', 'site_url']);
            $config = [
                'appId' => $alipay['ali_pay_appid'] ?? '',
                'merchantPrivateKey' => $alipay['alipay_merchant_private_key'] ?? '',
                'alipayPublicKey' => $alipay['alipay_public_key'] ?? '',
                'notifyUrl' => ($alipay['site_url'] ?? '') . Url::buildUrl('/api/pay/notify/alipay'),
            ];
        }
        $this->config = array_merge($this->config, $config);
		Factory::setOptions($this->getOptions());
        $this->response = new ResponseChecker();
    }

    /**
     * 实例化
     * @param array $config
     * @return static
     */
    public static function instance(array $config = []): self
    {
        if (is_null(self::$instance)) {
            self::$instance = new static($config);
        }
        return self::$instance;
    }

    /**
     * 是否是支付宝付款码
     * @param string $authCode
     * @return bool
     */
    public static function isAliPayAuthCode(string $authCode): bool
    {
        return preg_match('/^[0-9]{16,24}$/', $authCode) && in_array(substr($authCode, 0, 2), ['25', '26', '27', '28', '29', '30']);
    }

    /**
     * 初始化
     */
    protected function initialize()
    {
        Factory::setOptions($this->getOptions());
    }

    /**
     * 设置配置
     * @return Config
     */
    protected function getOptions(): Config
    {
        $options = new Config();
        $options->protocol = 'https';
        $options->gatewayHost = 'openapi.alipay.com';
        $options->signType = 'RSA2';

        $options->appId = $this->config['appId'];
        // 为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中
        $options->merchantPrivateKey = $this->config['merchantPrivateKey'];
        //注：如果采用非证书模式，则无需赋值上面的三个证书路径，改为赋值如下的支付宝公钥字符串即可
        $options->alipayPublicKey = $this->config['alipayPublicKey'];
        //可设置异步通知接收服务地址（可选）
        $options->notifyUrl = $this->config['notifyUrl'];
        //可设置AES密钥，调用AES加解密相关接口时需要（可选）
        if ($this->config['encryptKey']) {
            $options->encryptKey = $this->config['encryptKey'];
        }
        Tea::config(['verify' => false]);

        return $options;
    }

    /**
     * 付款码付款
     * @param string $authCode
     * @param string $title
     * @param string $orderId
     * @param string $totalAmount
     * @param string $passbackParams
     * @return array
     */
    #[ArrayShape([
        'paid' => "int",
        'message' => "mixed|string",
        'payInfo' => "array"
    ])]
    public function microPay(string $authCode, string $title, string $orderId, string $totalAmount, string $passbackParams): array
    {
        $title = trim($title);
        try {
            $result = Factory::payment()->faceToFace()->optional('passback_params', $passbackParams)->pay($title, $orderId, $totalAmount, $authCode);
            if ($this->response->success($result)) {
                $response = $result->toMap();
                return [
                    'paid' => $response['code'] === '10000' ? 1 : 0,
                    'message' => $response['sub_msg'] ?? '支付成功',
                    'payInfo' => $response
                ];
            } else {
                throw new PayException('失败原因:' . $result->msg . ',' . $result->subMsg);
            }
        } catch (\Exception $e) {
            throw new PayException($e->getMessage());
        }
    }

    /**
     * 创建订单
     * @param string $title 商品名称
     * @param string $orderId 订单号
     * @param string $totalAmount 支付金额
     * @param string $passbackParams 备注
     * @param string $quitUrl 同步跳转地址
     * @param string $siteUrl
     * @param bool $isCode
     */
    public function create(string $title, string $orderId, string $totalAmount, string $passbackParams, string $quitUrl = '', string $siteUrl = '', bool $isCode = false)
    {
        $title = trim($title);
        try {
            if ($isCode) {
                //二维码支付
                $result = Factory::payment()->faceToFace()->optional('passback_params', $passbackParams)->precreate($title, $orderId, $totalAmount);
            } else if (request()->isApp()) {
                //app支付
                $result = Factory::payment()->app()->optional('passback_params', $passbackParams)->pay($title, $orderId, $totalAmount);
            } else {
                //h5支付
                $result = Factory::payment()->wap()->optional('passback_params', $passbackParams)->pay($title, $orderId, $totalAmount, $quitUrl, $siteUrl);
            }
            if ($this->response->success($result)) {
                return $result->body ?? $result;
            } else {
                throw new PayException('失败原因:' . $result->msg . ',' . $result->subMsg);
            }
        } catch (\Exception $e) {
            throw new PayException($e->getMessage());
        }
    }

    /**
     * 订单退款
     * @param string $outTradeNo 订单号
     * @param string $totalAmount 退款金额
     * @param string $refund_id 退款单号
     * @return AlipayTradeRefundResponse
     */
    public function refund(string $outTradeNo, string $totalAmount, string $refund_id): AlipayTradeRefundResponse
    {
        try {
            $result = Factory::payment()->common()->refund($outTradeNo, $totalAmount, $refund_id);
            if ($this->response->success($result)) {
                return $result;
            } else {
                throw new PayException('失败原因:' . $result->msg . ',' . $result->subMsg);
            }
        } catch (\Exception $e) {
            throw new PayException($e->getMessage());
        }
    }

    /**
     * 查询交易退款单号信息
     * @param string $outTradeNo
     * @param string $outRequestNo
     * @return AlipayTradeFastpayRefundQueryResponse
     */
    public function queryRefund(string $outTradeNo, string $outRequestNo): AlipayTradeFastpayRefundQueryResponse
    {
        try {
            $result = Factory::payment()->common()->queryRefund($outTradeNo, $outRequestNo);
            if ($this->response->success($result)) {
                return $result;
            } else {
                throw new PayException('失败原因:' . $result->msg . ',' . $result->subMsg);
            }
        } catch (\Exception $e) {
            throw new PayException($e->getMessage());
        }
    }

    /**
     * 支付异步回调
     * @return string
     */
    public static function handleNotify()
    {
        return self::instance()->notify(function ($notify) {
            if (isset($notify->out_trade_no)) {
                if (isset($notify->attach) && $notify->attach) {
                    if (($count = strpos($notify->out_trade_no, '_')) !== false) {
                        $notify->trade_no = $notify->out_trade_no;
                        $notify->out_trade_no = substr($notify->out_trade_no, $count + 1);
                    }
                    return (new Hook(PayNotifyServices::class, 'aliyun'))->listen($notify->attach, $notify->out_trade_no, $notify->trade_no);
                }
                return false;
            }
        });
    }

    /**
     * 异步回调
     * @param callable $notifyFn
     * @return string
     */
    public function notify(callable $notifyFn)
    {
//        app()->request->filter(['trim']);
//        $paramInfo = app()->request->postMore([
//            ['gmt_create', ''],
//            ['charset', ''],
//            ['seller_email', ''],
//            ['subject', ''],
//            ['sign', ''],
////            ['buyer_id', ''],
//            ['invoice_amount', ''],
//            ['notify_id', ''],
//            ['fund_bill_list', ''],
//            ['notify_type', ''],
//            ['trade_status', ''],
//            ['receipt_amount', ''],
//            ['buyer_pay_amount', ''],
//            ['app_id', ''],
//            ['seller_id', ''],
//            ['sign_type', ''],
//            ['gmt_payment', ''],
//            ['notify_time', ''],
//            ['passback_params', ''],
//            ['version', ''],
//            ['out_trade_no', ''],
//            ['total_amount', ''],
//            ['trade_no', ''],
//            ['auth_app_id', ''],
//            ['buyer_logon_id', ''],
//            ['point_amount', ''],
//        ], false, false);
        $paramInfo = app()->request->param();
        unset($paramInfo['type']);
		//卡劵红包类
//		$voucher_detail_list = app()->request->param('voucher_detail_list');
//		if ($voucher_detail_list) {
//			$paramInfo['voucher_detail_list'] = $voucher_detail_list;
//		}
        //商户订单号
        $postOrder['out_trade_no'] = $paramInfo['out_trade_no'] ?? '';
        //支付宝交易号
        $postOrder['trade_no'] = $paramInfo['trade_no'] ?? '';
        //交易状态
        $postOrder['trade_status'] = $paramInfo['trade_status'] ?? '';
        //备注
        $postOrder['attach'] = isset($paramInfo['passback_params']) ? urldecode($paramInfo['passback_params']) : '';

        if (in_array($postOrder['trade_status'], ['TRADE_SUCCESS', 'TRADE_FINISHED']) && $this->verifyNotify($paramInfo)) {
            try {
                if ($notifyFn((object)$postOrder)) {
                    return 'success';
                }
            } catch (\Exception $e) {
                Log::error('支付宝异步会回调成功,执行函数错误。错误单号：' . $postOrder['out_trade_no']);
            }
        }
        return 'fail';

    }

    /**
     * 验签
     * @param array $param
     * @return bool
     */
    protected function verifyNotify(array $param): bool
    {
        try {
            return Factory::payment()->common()->verifyNotify($param);
        } catch (\Exception $e) {
            Log::error('支付宝回调成功,验签发生错误，错误原因:' . $e->getMessage());
        }
        return false;
    }

    /**
     * 商家支付接口
     * @param array $bizParams
     * @return \Alipay\EasySDK\Util\Generic\Models\AlipayOpenApiGenericResponse|false
     * User: liusl
     * DateTime: 2024/10/21 12:01
     */
    public function merchantPay(array $bizParams)
    {
        try {
            // 调用工厂类的通用方法执行支付宝转账操作
            $result = Factory::util()->generic()->execute('alipay.fund.trans.toaccount.transfer', [], $bizParams);
            // 判断支付是否成功
            if ($this->response->success($result)) {
                return $result;
            } else {
                return false;
            }
        } catch (\Exception $e) {
            // 记录日志并返回false
            Log::error('支付宝转账失败，失败原因:' . $e->getMessage());
            return false;
        }
    }
}
