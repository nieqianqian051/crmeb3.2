// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2021 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------
import request from '@/plugins/request';

/**
 * 获取版权信息
 */
export function copyrightInfoApi () {
    return request({
        url: '/copyright',
        method: 'get'
    });
}

/*
 * 登录
 * */
export function AccountLogin (data) {
    return request({
        url: '/login',
        method: 'post',
        data
    });
}

/**
 * 退出登陆
 * @constructor
 */
export function AccountLogout () {
    return request({
        url: '/logout',
        method: 'get'
    });
}

/**
 * 获取轮播图和logo
 */
export function loginInfoApi () {
    return request({
        url: '/login/info',
        method: 'get'
    });
}

/**
 * 获取菜单数据
 */
export function menusApi () {
    return request({
        url: '/menus',
        method: 'get'
    });
}

/**
 * 搜索菜单数据
 */
export function menusListApi () {
    return request({
        url: '/menusList',
        method: 'get'
    });
}

export function getSysInfo () {
    return request({
        url: '/logo',
        method: 'get'
    });
}


/**
 * 获取底部版权信息
 */
export function copyright () {
    return request({
        url: '/copyright',
        method: 'get'
    });
}

export function isCaptcha (data) {
  return request({
      url: '/is_captcha',
      method: 'post',
      data
  });
}

export function AccountRegister(){
    return {}
}
