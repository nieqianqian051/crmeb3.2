<template>
	<view :style="colorStyle">
		<form @submit="editPwd">
			<view class="ChangePassword">
				<view class="list">
					<view class="item">
						<input type='number' placeholder='填写手机号码' placeholder-class='placeholder' v-model="phone"></input>
					</view>
					<view class="item acea-row row-between-wrapper">
						<input type='number' placeholder='填写验证码' placeholder-class='placeholder' class="codeIput" v-model="captcha"></input>
						<button class="code font-num" :class="disabled === true ? 'on' : ''" :disabled='disabled' @click="code">
							{{ text }}
						</button>
					</view>
				</view>
				<button form-type="submit" class="confirmBnt bg-color">确认绑定</button>
			</view>
		</form>
		<Verify @success="success"  :captchaType="'blockPuzzle'"
			:imgSize="{ width: '330px', height: '155px' }" ref="verify"></Verify>
		<!-- #ifdef MP -->
		<authorize v-if="isShowAuth" @authColse="authColse" @onLoadFun="onLoadFun"></authorize>
		<!-- #endif -->	
	</view>
</template>

<script>
	import sendVerifyCode from "@/mixins/SendVerifyCode";
	import Verify from '../components/verify/verify.vue';
	import {
		registerVerify,
		bindingUserPhone,
		verifyCode,
		updatePhone
	} from '@/api/api.js';
	import {
		toLogin
	} from '@/libs/login.js';
	import {
		mapGetters
	} from "vuex";
	import colors from '@/mixins/color.js';
	export default {
		mixins: [sendVerifyCode,colors],
		components: {
			Verify
		},
		data() {
			return {
				phone:'',
				captcha:'',
				isAuto: false, //没有授权的不会自动授权
				isShowAuth: false, //是否隐藏授权
				key: '',
				authKey:'',
				type:0,
				show: false
			};
		},
		computed: mapGetters(['isLogin']),
		onLoad(options) {
			if (this.isLogin) {
				this.getVerifyCode();
			} else {
				toLogin()
			}
			this.authKey = options.key || '';
			this.url = options.url || '';
			this.type = options.type || 0
		},
		onShow() {
			uni.removeStorageSync('form_type_cart');
		},
		methods: {
			onLoadFun:function(){
				this.getVerifyCode();
				this.isShowAuth = false
			},
			// 授权关闭
			authColse: function(e) {
				this.isShowAuth = e
			},
			getVerifyCode(){
				verifyCode().then(res=>{
					this.$set(this, 'key', res.data.key)
				});
			},
			editPwd: function() {
				let that = this;
				if (!that.phone) return that.$util.Tips({
					title: '请填写手机号码！'
				});
				if (!(/^1(3|4|5|7|8|9|6)\d{9}$/i.test(that.phone))) return that.$util.Tips({
					title: '请输入正确的手机号码！'
				});
				if (!that.captcha) return that.$util.Tips({
					title: '请填写验证码'
				});
				if(this.type == 0){
					bindingUserPhone({
						phone: that.phone,
						captcha: that.captcha
					}).then(res => {
						if (res.data !== undefined && res.data.is_bind) {
							uni.showModal({
								title: '是否绑定账号',
								content: res.msg,
								confirmText: '绑定',
								success(res) {
									if (res.confirm) {
										bindingUserPhone({
											phone: that.phone,
											captcha: that.captcha,
											step: 1
										}).then(res => {
											return that.$util.Tips({
												title: res.msg,
												icon: 'success'
											}, {
												tab: 5,
												url: '/pages/users/user_info/index'
											});
										}).catch(err => {
											return that.$util.Tips({
												title: err
											});
										})
									} else if (res.cancel) {
										return that.$util.Tips({
											title: '您已取消绑定！'
										}, {
											tab: 5,
											url: '/pages/users/user_info/index'
										});
									}
								}
							});
						} else
							return that.$util.Tips({
								title: '绑定成功',
								icon: 'success'
							}, {
								tab: 5,
								url: '/pages/users/user_info/index'
							});
					}).catch(err => {
						return that.$util.Tips({
							title: err
						});
					})
				}else{
					updatePhone({
						phone: that.phone,
						captcha: that.captcha,
					}).then(res=>{
						return that.$util.Tips({
							title: res.msg,
							icon: 'success'
						}, {
							tab: 5,
							url: '/pages/users/user_info/index'
						});
					}).catch(error=>{
						return that.$util.Tips({
							title: error,
						});
					})
				}	
			},
			success(data){
				this.$refs.verify.hide()
				let that = this;
				verifyCode().then(res=>{
					registerVerify(
					{
						phone: that.phone,
						type: 'reset', 
						key: res.data.key,
						captchaType: 'blockPuzzle',
						captchaVerification: data.captchaVerification,
					}
					).then(res => {
						that.$util.Tips({
							title: res.msg
						});
						that.sendCode();
					}).catch(err => {
						return that.$util.Tips({
							title: err
						});
					});
				});
					
			},
			/**
			 * 发送验证码
			 * 
			 */
			code() {
				// let that = this;
				if (!this.phone) return this.$util.Tips({
					title: '请填写手机号码！'
				});
				if (!(/^1(3|4|5|7|8|9|6)\d{9}$/i.test(this.phone))) return this.$util.Tips({
					title: '请输入正确的手机号码！'
				});
		
					this.$refs.verify.show();
			

				return;
			},
		}
	}
</script>

<style lang="scss">
	page {
		background-color: #fff !important;
	}

	.ChangePassword .phone {
		font-size: 30rpx;
		font-weight: bold;
		text-align: center;
		margin-top: 55rpx;
	}

	.ChangePassword .list {
		width: 580rpx;
		margin: 110rpx auto 0 auto;
	}

	.ChangePassword .list .item {
		width: 100%;
		height: 88rpx;
		background: #F5F5F5;
		border-radius: 24px 24px 24px 24px;
		padding: 24rpx 32rpx;
		margin-bottom: 40rpx;
	}

	.ChangePassword .list .item input {
		width: 100%;
		height: 100%;
		font-size: 30rpx;

	}

	.ChangePassword .list .item .placeholder {
		color: #b9b9bc;
	}

	.ChangePassword .list .item input.codeIput {
		width: 340rpx;
	}

	.ChangePassword .list .item .code {
		font-size: 30rpx;
		background-color: #F5F5F5;
	}

	.ChangePassword .list .item .code.on {
		color: #b9b9bc !important;
	}

	.ChangePassword .confirmBnt {
		font-size: 30rpx;
		width: 580rpx;
		height: 90rpx;
		border-radius: 45rpx;
		color: #fff;
		margin: 82rpx auto 0 auto;
		text-align: center;
		line-height: 90rpx;
	}
</style>
