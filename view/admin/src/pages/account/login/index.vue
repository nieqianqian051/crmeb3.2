<template>
  <!-- 登录 -->
  <div class="page-account">
    <div
      class="container"
      :class="[fullWidth > 768 ? 'containerSamll' : 'containerBig']"
    >
      <el-carousel
        ref="carousel"
        :interval="4000"
        :autoplay="true"
        trigger="click"
        height="420px"
        class="swiperPross"
      >
        <el-carousel-item v-for="(item, index) in swiperList" :key="index">
          <img :src="item" />
        </el-carousel-item>
      </el-carousel>
      <div class="index_from page-account-container" v-if="system_secure_type">
        <div class="page-account-top mb-20">
          <div class="page-account-top-logo">
            <img :src="login_logo" alt="logo" />
          </div>
        </div>
        <Form
          ref="formInline"
          :model="formInline"
          :rules="ruleInline"
          @keyup.enter="getSecure()"
          v-if="secureStep == 0"
        >
          <FormItem prop="username">
            <Input
              type="text"
              v-model="formInline.username"
              prefix="ios-contact-outline"
              placeholder="请输入用户名"
              size="large"
            />
          </FormItem>
          <FormItem prop="password">
            <Input
              type="password"
              v-model="formInline.password"
              prefix="ios-lock-outline"
              placeholder="请输入密码"
              size="large"
            />
          </FormItem>
          <FormItem>
            <Button
              type="primary"
              long
              size="large"
              @click="getSecure()"
              class="btn"
              >下一步</Button
            >
          </FormItem>
          <!-- <div class="primary" @click="resetUpwd()" v-show="resetStatus">忘记密码</div> -->
        </Form>
        <Form
          ref="formInline"
          :model="formInline"
          :rules="ruleInline"
          @keyup.enter="secureChange()"
          v-if="secureStep == 1"
        >
          <FormItem prop="phone">
            <Input
              style="width:100%"
              maxlength="11"
              type="text"
              disabled
              v-model="formInline.phone"
              prefix="ios-contact-outline"
              placeholder="请输入手机号"
              size="large"
            />
          </FormItem>
          <FormItem prop="code">
            <div class="code">
              <Input
                type="text"
                v-model="formInline.code"
                prefix="ios-keypad-outline"
                placeholder="请输入验证码"
                size="large"
                class="captch_input"
              />
              <Button
                class="send_code"
                :disabled="disabled"
                size="large"
                @click="code"
                >{{ text }}</Button
              >
            </div>
          </FormItem>
          <FormItem>
            <Button
              type="primary"
              long
              size="large"
              @click="secureChange()"
              class="btn"
              >登录</Button
            >
          </FormItem>
        </Form>
      </div>
      <div class="index_from page-account-container" v-else>
        <div class="page-account-top ">
          <div class="page-account-top-logo">
            <img :src="login_logo" alt="logo" />
          </div>
        </div>
        <div v-if="this.resetStatus" class="login_tab flex">
          <div
            class="login_tab_item"
            v-for="(item, index) in loginTab"
            :key="index"
            :class="active == index ? 'active_tab' : ''"
            @click="loginTabSwitch(index)"
          >
            {{ item }}
          </div>
        </div>
        <Form
          ref="formInline"
          :model="formInline"
          :rules="ruleInline"
          @keyup.enter="handleSubmit('formInline')"
          v-show="!active && resetStatus"
        >
          <FormItem prop="username">
            <Input
              type="text"
              v-model="formInline.username"
              prefix="ios-contact-outline"
              placeholder="请输入用户名"
              size="large"
            />
          </FormItem>
          <FormItem prop="password">
            <Input
              type="password"
              v-model="formInline.password"
              prefix="ios-lock-outline"
              placeholder="请输入密码"
              size="large"
            />
          </FormItem>
          <!-- <FormItem prop="code">
                        <div class="code">
                            <Input type="text" v-model="formInline.code" prefix="ios-keypad-outline"
                                placeholder="请输入验证码" size="large" />
                            <img :src="imgcode" class="pictrue" @click="captchas" />
                        </div>
                    </FormItem> -->
          <FormItem>
            <Button
              type="primary"
              long
              size="large"
              @click="handleSubmit('formInline')"
              class="btn"
              >登录</Button
            >
          </FormItem>
          <div class="primary" @click="resetUpwd()" v-show="resetStatus">
            忘记密码
          </div>
        </Form>
        <Form
          ref="formInline"
          :model="formInline"
          :rules="ruleInline"
          @keyup.enter="handleSubmit('formInline')"
          v-show="active && resetStatus"
        >
          <FormItem prop="phone">
            <Input
              style="width:100%"
              maxlength="11"
              type="text"
              v-model="formInline.phone"
              prefix="ios-contact-outline"
              placeholder="请输入手机号"
              size="large"
            />
          </FormItem>
          <FormItem prop="code">
            <div class="code">
              <Input
                type="text"
                v-model="formInline.code"
                prefix="ios-keypad-outline"
                placeholder="请输入验证码"
                size="large"
                class="captch_input"
              />
              <!-- 手机号登录 -->
              <Button
                class="send_code"
                :disabled="disabled"
                size="large"
                @click="code"
                >{{ text }}</Button
              >
            </div>
          </FormItem>
          <FormItem>
            <Button
              type="primary"
              long
              size="large"
              @click="handleSubmit('formInline')"
              class="btn"
              >{{ $t("page.login.submit") }}
            </Button>
          </FormItem>
        </Form>
        <Form
          ref="formInline"
          :model="formInline"
          :rules="ruleInline"
          @keyup.enter="handleSubmit('formInline')"
          v-show="!resetStatus"
        >
          <FormItem>
            <Input
              type="text"
              maxlength="11"
              v-model="formInline.phone"
              prefix="ios-contact-outline"
              placeholder="请输入手机号"
              size="large"
            />
          </FormItem>
          <FormItem prop="code">
            <div class="code">
              <Input
                type="text"
                v-model="formInline.code"
                prefix="ios-keypad-outline"
                placeholder="请输入验证码"
                size="large"
                class="captch_input"
              />
              <Button
                class="send_code"
                :disabled="disabled"
                size="large"
                @click="code"
                >{{ text }}</Button
              >
            </div>
          </FormItem>
          <FormItem>
            <Input
              type="text"
              v-model="formInline.new_pwd"
              prefix="ios-contact-outline"
              placeholder="请输入密码"
              size="large"
            />
          </FormItem>
          <FormItem>
            <Button
              type="primary"
              long
              size="large"
              @click="resetPwd('formInline')"
              class="btn"
              >提交</Button
            >
          </FormItem>
          <div class="primary" @click="resetUpwd()" v-show="!resetStatus">
            返回登录
          </div>
        </Form>
      </div>
    </div>
    <!-- <vcode :show="isShow" @success="onSuccess()" @close="onClose()" successText="行为验证通过" /> -->
    <Verify
      @success="success"
      captchaType="blockPuzzle"
      :imgSize="{ width: '330px', height: '155px' }"
      ref="verify"
    ></Verify>
    <div class="footer">
      <div class="pull-right" v-if="copyright">{{ copyright }}</div>
      <div class="pull-right" v-else>
        Copyright ©2014-2024
        <a class="infoUrl" href="https://www.crmeb.com" target="_blank">{{
          version
        }}</a>
      </div>
    </div>
    <!--  :imgs="[Img1, Img2]" 支持自定义背景图片，详见 https://juejin.cn/post/6978777429447966757 -->
  </div>
</template>
<script>
import {
  AccountLogin,
  loginInfoApi,
  mobilLogin,
  resetPassword,
  copyrightInfoApi,
  isCaptcha,
  loginSecureApi,
} from "@/api/account";
import {
  getHeaderName,
  getHeaderSider,
  getMenuSider,
  getSiderSubmenu,
} from "@/libs/system";
import mixins from "../mixins";
import Setting from "@/setting";
import util from "@/libs/util";
import axios from "axios";
// import Vcode from "vue-puzzle-vcode";
import Verify from "@/components/verifition/Verify";
export default {
  mixins: [mixins],
  components: {
    Verify,
    // Vcode
  },
  data() {
    return {
      fullWidth: document.documentElement.clientWidth,
      swiperOption: {
        pagination: ".swiper-pagination",
        autoplay: true,
      },
      isShow: false, // 验证码模态框是否出现
      isCaptcha: false,
      loading: false,
      autoLogin: true,
      imgcode: "",
      formInline: {
        username: "",
        password: "",
        phone: "",
        // code: '',
        new_pwd: "",
      },
      ruleInline: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
        ],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
        // code: [
        //     { required: true, message: '请输入验证码', trigger: 'blur' }
        // ],
        phone: [
          { required: true, message: "请填写手机号码", trigger: "change" },
          {
            pattern: /^1[3456789]\d{9}$/,
            message: "手机号码格式不正确",
            trigger: "change",
          },
        ],
      },
      errorNum: 0,
      login_logo: "",
      swiperList: [],
      defaultSwiperList: require("@/assets/images/sw.jpg"),
      loginTab: ["账号登录", "短信登录"],
      active: 0,
      isSms: false,
      disabled: false,
      text: "获取验证码",
      resetStatus: true,
      copyright: "",
      version: "",
      system_secure_type: 0,
      secureStep: 0,
      secure_key: "",
    };
  },
  created() {
    var _this = this;
    top != window && (top.location.href = location.href);
    document.onkeydown = function(e) {
      if (_this.$route.name === "login") {
        let key = window.event.keyCode;
        if (key === 13) {
          if (_this.secureStep == 0 && _this.system_secure_type) {
            _this.getSecure();
          } else if (_this.secureStep == 1 && _this.system_secure_type) {
            _this.secureChange();
          } else {
            _this.handleSubmit("formInline");
          }
        }
      }
    };
    window.addEventListener("resize", this.handleResize);
  },
  watch: {
    fullWidth(val) {
      // 为了避免频繁触发resize函数导致页面卡顿，使用定时器
      if (!this.timer) {
        // 一旦监听到的screenWidth值改变，就将其重新赋给data里的screenWidth
        this.screenWidth = val;
        this.timer = true;
        let that = this;
        setTimeout(function() {
          // 打印screenWidth变化的值
          that.timer = false;
        }, 400);
      }
    },
    $route(n) {
      this.captchas();
    },
  },
  mounted: function() {
    this.$nextTick(() => {
      if (this.screenWidth < 768) {
        document.getElementsByTagName("canvas")[0]
          ? document
              .getElementsByTagName("canvas")[0]
              .removeAttribute("class", "index_bg")
          : "";
      } else {
        document.getElementsByTagName("canvas")[0]
          ? (document.getElementsByTagName("canvas")[0].className = "index_bg")
          : "";
      }
      this.swiperData();
    });
    this.captchas();
    this.getCopyright();
  },
  methods: {
    //切换登录方式
    loginTabSwitch(index) {
      this.active = index;
      this.formInline.code = "";
    },
    //发送验证码

    sendCode(params) {
      if (this.disabled) {
        this.closeModel(params);
      }
      // if (this.disabled) return;
      this.disabled = true;
      let n = 60;
      this.text = "剩余 " + n + "s";
      const run = setInterval(() => {
        n = n - 1;
        if (n < 0) {
          clearInterval(run);
        }
        this.text = "剩余 " + n + "s";
        if (this.text < "剩余 " + 0 + "s") {
          this.disabled = false;
          this.text = "重新获取";
        }
      }, 1000);
    },
    code() {
      this.isSms = true;
      // if (this.system_secure_type && this.secure_key) {
        
      // } else {
        if (!this.formInline.phone) return this.$Message.error("请填写手机号码");
        if (!/^1(3|4|5|7|8|9|6)\d{9}$/i.test(this.formInline.phone)) return this.$Message.error("请输入正确的手机号码");
        this.$refs.verify.show();
      // }
    },
    //忘记密码
    resetUpwd() {
      if (this.resetStatus) {
        this.$set(this, "loginTab", ["忘记密码"]);
        this.resetStatus = false;
      } else {
        this.$set(this, "loginTab", ["账号登录", "短信登录"]);
        this.resetStatus = true;
        this.active = 0;
      }
      this.$refs["formInline"].resetFields();
    },
    swiperData() {
      loginInfoApi()
        .then((res) => {
          let data = res.data || {};
          this.login_logo = data.login_logo
            ? data.login_logo
            : require("@/assets/images/logo.png");
          this.swiperList = data.slide.length
            ? data.slide
            : [this.defaultSwiperList];
          this.system_secure_type = data.system_secure_type
            ? data.system_secure_type
            : 0;
          // this.swiperList = data.slide.length ? data.slide : [{ slide: this.defaultSwiperList }];
          this.$cache.local.setJSON("file_size_max", data.upload_file_size_max);
        })
        .catch((res) => {
          this.$Message.error(res.msg);
        });
    },
    getChilden(data) {
      if (data.length && data[0].children) {
        return this.getChilden(data[0].children);
      }
      return data[0].path;
    },
    getCopyright() {
      copyrightInfoApi()
        .then((res) => {
          let data = res.data;
          this.copyright = data.copyrightContext;
          this.version = data.version;
        })
        .catch((res) => {
          this.$Message.error(res.msg);
        });
    },
    // 关闭模态框
    closeModel(params) {
      if (this.resetStatus == false) {
        if (this.formInline.phone == "")
          return this.$Message.error("手机号不能为空");
        if (this.formInline.new_pwd == "")
          return this.$Message.error("新密码不能为空");
        resetPassword({
          phone: this.formInline.phone,
          code: this.formInline.code,
          new_pwd: this.formInline.new_pwd,
          // captchaType: 'blockPuzzle',
          // captchaVerification: params.captchaVerification,
        })
          .then((res) => {
            this.$Message.success(res.msg);
            this.$set(this, "loginTab", ["账号登录", "短信登录"]);
            this.resetStatus = true;
            this.active = 0;
            this.$refs["formInline"].resetFields();
          })
          .catch((err) => {
            this.$Message.error(err.msg);
          });
      } else if (this.resetStatus == true && this.active == 0) {
        if (this.formInline.username == "")
          return this.$Message.error("账号不能为空");
        if (this.formInline.password == "")
          return this.$Message.error("密码不能为空");
        let msg = this.$Message.loading({
          content: "登录中...",
          duration: 0,
        });
        AccountLogin({
          account: this.formInline.username,
          pwd: this.formInline.password,
          // imgcode: this.formInline.code,
          captchaType: "blockPuzzle",
          captchaVerification: params.captchaVerification,
        })
          .then(async (res) => {
            msg();
            if (!res.data.unique_auth.length)
              return this.$Message.error("您暂无任何菜单权限");
            this.$store.dispatch("admin/account/setPageTitle");
            let expires = res.data.expires_time;
            // 记录用户登陆信息
            util.cookies.set("uuid", res.data.user_info.id, {
              expires: expires,
            });
            util.cookies.set("token", res.data.token, {
              expires: expires,
            });
            util.cookies.set("expires_time", res.data.expires_time, {
              expires: expires,
            });
            const db = await this.$store.dispatch("admin/db/database", {
              user: true,
            });
            // 保存菜单信息
            // db.set('menus', res.data.menus).set('unique_auth', res.data.unique_auth).set('user_info', res.data.user_info).write();
            db.set("unique_auth", res.data.unique_auth)
              .set("user_info", res.data.user_info)
              .write();
            const menuSider = res.data.menus;
            this.$store.commit("admin/menus/getmenusNav", menuSider);
            let headerSider = getHeaderSider(res.data.menus);
            this.$store.commit("admin/menu/setHeader", headerSider);
            let toPath = this.getChilden(res.data.menus);
            // 获取侧边栏菜单
            const headerName = getHeaderName(
              {
                path: toPath,
                query: {},
                params: {},
              },
              menuSider
            );
            const filterMenuSider = getMenuSider(menuSider, headerName);
            // 指定当前显示的侧边菜单
            this.$store.commit(
              "admin/menu/setSider",
              filterMenuSider[0].children
            );
            //设置首页path
            this.$store.commit("admin/menus/setIndexPath", toPath);
            // 记录用户信息
            this.$store.dispatch("admin/user/set", {
              name: res.data.user_info.account,
              avatar: res.data.user_info.head_pic,
              access: res.data.unique_auth,
              logo: res.data.logo,
              logoSmall: res.data.logo_square,
              version: res.data.version,
              newOrderAudioLink: res.data.newOrderAudioLink,
              division_id:res.data.user_info.division_id
            });
            // if (this.jigsaw) this.jigsaw.reset();

            return this.$router.replace({
              path: this.$route.query.redirect || toPath || "/admin/",
            });
          })
          .catch((res) => {
            msg();
            let data = res === undefined ? {} : res;
            this.errorNum++;
            this.captchas();
            this.$Message.error(data.msg || "登录失败");
          });
      } else {
        let msg = this.$Message.loading({
          content: "登录中...",
          duration: 0,
        });
        mobilLogin({
          phone: this.formInline.phone,
          secure_key: "",
          code: this.formInline.code,
        })
          .then(async (res) => {
            msg();
            if (!res.data.unique_auth.length)
              return this.$Message.error("您暂无任何菜单权限");
            this.$store.dispatch("admin/account/setPageTitle");
            let expires = res.data.expires_time;
            // 记录用户登陆信息
            util.cookies.set("uuid", res.data.user_info.id, {
              expires: expires,
            });
            util.cookies.set("token", res.data.token, {
              expires: expires,
            });
            util.cookies.set("expires_time", res.data.expires_time, {
              expires: expires,
            });
            const db = await this.$store.dispatch("admin/db/database", {
              user: true,
            });
            // 保存菜单信息
            // db.set('menus', res.data.menus).set('unique_auth', res.data.unique_auth).set('user_info', res.data.user_info).write();
            db.set("unique_auth", res.data.unique_auth)
              .set("user_info", res.data.user_info)
              .write();

            const menuSider = res.data.menus;
            this.$store.commit("admin/menus/getmenusNav", menuSider);
            let headerSider = getHeaderSider(res.data.menus);
            this.$store.commit("admin/menu/setHeader", headerSider);
            let toPath = this.getChilden(res.data.menus);
            // 获取侧边栏菜单
            const headerName = getHeaderName(
              {
                path: toPath,
                query: {},
                params: {},
              },
              menuSider
            );
            const filterMenuSider = getMenuSider(menuSider, headerName);
            // 指定当前显示的侧边菜单
            this.$store.commit(
              "admin/menu/setSider",
              filterMenuSider[0].children
            );
            //设置首页path
            this.$store.commit("admin/menus/setIndexPath", toPath);
            // 记录用户信息
            this.$store.dispatch("admin/user/set", {
              name: res.data.user_info.account,
              avatar: res.data.user_info.head_pic,
              access: res.data.unique_auth,
              logo: res.data.logo,
              logoSmall: res.data.logo_square,
              version: res.data.version,
              newOrderAudioLink: res.data.newOrderAudioLink,
              division_id:res.data.user_info.division_id
            });
            return this.$router.replace({
              path: this.$route.query.redirect || toPath || "/admin/",
            });
          })
          .catch((res) => {
            msg();
            let data = res === undefined ? {} : res;
            this.errorNum++;
            this.captchas();
            this.$Message.error(data.msg || "登录失败");
          });
      }
    },
    getExpiresTime(expiresTime) {
      let nowTimeNum = Math.round(new Date() / 1000);
      let expiresTimeNum = expiresTime - nowTimeNum;
      return parseFloat(parseFloat(parseFloat(expiresTimeNum / 60) / 60) / 24);
    },
    handleResize(event) {
      this.fullWidth = document.documentElement.clientWidth;
      if (this.fullWidth < 768) {
        document
          .getElementsByTagName("canvas")[0]
          .removeAttribute("class", "index_bg");
      } else {
        document.getElementsByTagName("canvas")[0].className = "index_bg";
      }
    },
    captchas: function() {
      this.imgcode =
        Setting.apiBaseURL + "/captcha_pro?" + Date.parse(new Date());
    },
    getLogin() {
      if (this.formInline.username == "")
        return this.$Message.error("账号不能为空");
      if (this.formInline.password == "")
        return this.$Message.error("密码不能为空");
      let msg = this.$Message.loading({
        content: "登录中...",
        duration: 0,
      });
      AccountLogin({
        account: this.formInline.username,
        pwd: this.formInline.password,
        // imgcode: this.formInline.code,
        captchaType: "",
        captchaVerification: "",
      })
        .then(async (res) => {
          msg();
          if (!res.data.unique_auth.length)
            return this.$Message.error("您暂无任何菜单权限");
          this.$store.dispatch("admin/account/setPageTitle");
          let expires = res.data.expires_time;
          // 记录用户登陆信息
          util.cookies.set("uuid", res.data.user_info.id, {
            expires: expires,
          });
          util.cookies.set("token", res.data.token, {
            expires: expires,
          });
          util.cookies.set("expires_time", res.data.expires_time, {
            expires: expires,
          });
          const db = await this.$store.dispatch("admin/db/database", {
            user: true,
          });
          // 保存菜单信息
          // db.set('menus', res.data.menus).set('unique_auth', res.data.unique_auth).set('user_info', res.data.user_info).write();
          db.set("unique_auth", res.data.unique_auth)
            .set("user_info", res.data.user_info)
            .write();
          const menuSider = res.data.menus;
          this.$store.commit("admin/menus/getmenusNav", menuSider);
          let headerSider = getHeaderSider(res.data.menus);
          this.$store.commit("admin/menu/setHeader", headerSider);
          let toPath = this.getChilden(res.data.menus);
          // 获取侧边栏菜单
          const headerName = getHeaderName(
            {
              path: toPath,
              query: {},
              params: {},
            },
            menuSider
          );
          const filterMenuSider = getMenuSider(menuSider, headerName);
          // 指定当前显示的侧边菜单
          this.$store.commit(
            "admin/menu/setSider",
            filterMenuSider[0].children
          );
          //设置首页path
          this.$store.commit("admin/menus/setIndexPath", toPath);
          // 记录用户信息
          this.$store.dispatch("admin/user/set", {
            name: res.data.user_info.account,
            avatar: res.data.user_info.head_pic,
            access: res.data.unique_auth,
            logo: res.data.logo,
            logoSmall: res.data.logo_square,
            version: res.data.version,
            newOrderAudioLink: res.data.newOrderAudioLink,
            division_id:res.data.user_info.division_id
          });
          // if (this.jigsaw) this.jigsaw.reset();

          return this.$router.replace({
            path: this.$route.query.redirect || toPath || "/admin/",
          });
        })
        .catch((res) => {
          msg();
          let data = res === undefined ? {} : res;
          this.errorNum++;
          this.captchas();
          this.$Message.error(data.msg || "登录失败");
        });
    },
    handleSubmit(name) {
      this.$refs[name].validate((valid) => {
        if (valid && this.active == 0) {
          isCaptcha({ account: this.formInline.username }).then((res) => {
            this.iscaptcha = res.data.is_captcha;
            if (this.iscaptcha === true) {
              this.$refs.verify.show();
            } else {
              this.getLogin();
            }
          });
        } else {
          this.closeModel();
        }
      });
    },
    getSecure() {
      if (this.formInline.username == "")
        return this.$Message.error("账号不能为空");
      if (this.formInline.password == "")
        return this.$Message.error("密码不能为空");
      loginSecureApi({
        account: this.formInline.username,
        pwd: this.formInline.password,
      })
        .then(async (res) => {
          this.formInline.phone = res.data.phone;
          this.secure_key = res.data.secure_key;
          this.secureStep = 1;
        })
        .catch((res) => {
          this.$Message.error(res.msg);
        });
    },
    secureChange() {
      //formInline.code
      if (this.formInline.code == "")
        return this.$Message.error("请输入短信验证码");
      let msg = this.$Message.loading({
        content: "登录中...",
        duration: 0,
      });
      mobilLogin({
        phone: "",
        secure_key: this.secure_key,
        code: this.formInline.code,
      })
        .then(async (res) => {
          msg();
          if (!res.data.unique_auth.length)
            return this.$Message.error("您暂无任何菜单权限");
          this.$store.dispatch("admin/account/setPageTitle");
          let expires = res.data.expires_time;
          // 记录用户登陆信息
          util.cookies.set("uuid", res.data.user_info.id, {
            expires: expires,
          });
          util.cookies.set("token", res.data.token, {
            expires: expires,
          });
          util.cookies.set("expires_time", res.data.expires_time, {
            expires: expires,
          });
          const db = await this.$store.dispatch("admin/db/database", {
            user: true,
          });
          // 保存菜单信息
          // db.set('menus', res.data.menus).set('unique_auth', res.data.unique_auth).set('user_info', res.data.user_info).write();
          db.set("unique_auth", res.data.unique_auth)
            .set("user_info", res.data.user_info)
            .write();

          const menuSider = res.data.menus;
          this.$store.commit("admin/menus/getmenusNav", menuSider);
          let headerSider = getHeaderSider(res.data.menus);
          this.$store.commit("admin/menu/setHeader", headerSider);
          let toPath = this.getChilden(res.data.menus);
          // 获取侧边栏菜单
          const headerName = getHeaderName(
            {
              path: toPath,
              query: {},
              params: {},
            },
            menuSider
          );
          const filterMenuSider = getMenuSider(menuSider, headerName);
          // 指定当前显示的侧边菜单
          this.$store.commit(
            "admin/menu/setSider",
            filterMenuSider[0].children
          );
          //设置首页path
          this.$store.commit("admin/menus/setIndexPath", toPath);
          // 记录用户信息
          this.$store.dispatch("admin/user/set", {
            name: res.data.user_info.account,
            avatar: res.data.user_info.head_pic,
            access: res.data.unique_auth,
            logo: res.data.logo,
            logoSmall: res.data.logo_square,
            version: res.data.version,
            newOrderAudioLink: res.data.newOrderAudioLink,
            division_id:res.data.user_info.division_id
          });
          return this.$router.replace({
            path: this.$route.query.redirect || toPath || "/admin/",
          });
        })
        .catch((res) => {
          msg();
          let data = res === undefined ? {} : res;
          this.errorNum++;
          this.captchas();
          this.$Message.error(data.msg || "登录失败");
        });
      // isCaptcha({ account: this.formInline.username}).then(res => {
      //     this.iscaptcha = res.data.is_captcha
      //     if (this.iscaptcha === true) {
      //       this.$refs.verify.show()
      //     } else {

      //     }
      //   })
    },
    resetPwd(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          this.$refs.verify.show();
          // if (this.formInline.phone == "") return this.$Message.error("手机号不能为空");
          // if (this.formInline.new_pwd == "") return this.$Message.error("新密码不能为空");
          // resetPassword({
          //     phone: this.formInline.phone,
          //     code: this.formInline.code,
          //     new_pwd: this.formInline.new_pwd,
          //     captchaType: 'blockPuzzle',
          //     captchaVerification: params.captchaVerification,
          // }).then(res => {
          //     this.$Message.success(res.msg);
          //     this.$set(this, 'loginTab', ['账号登录', '短信登录']);
          //     this.resetStatus = true;
          //     this.active = 0;
          //     this.$refs['formInline'].resetFields();
          // }).catch(err => {
          //     this.$Message.error(err.msg)
          // })
        }
      });
    },
    success(params) {
      if (this.isSms) {
        let verification = Setting.apiBaseURL.replace(/adminapi/, "api");
        axios.get(verification + "/verify_code").then((res) => {
          let codeData = {
            type: "login",
            phone: this.formInline.phone,
            key: res.data.data.key,
            code: "",
            captchaType: "blockPuzzle",
            captchaVerification: params.captchaVerification,
            secure_key: this.secure_key,
          };
          if (this.system_secure_type) {
            codeData.phone = "";
          }
          fetch(Setting.apiBaseURL + "/verify", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(codeData),
          })
            .then((response) => {
              this.sendCode();
            })
            .catch((err) => {
              this.$Message.error(err.msg);
            });
        });
      } else {
        this.closeModel(params);
        //可执行通过验证后的操作
      }
    },
    // 用户点击遮罩层，应该关闭模态框
    onClose() {
      this.isShow = false;
    },
  },
  beforeCreate() {
    if (this.fullWidth < 768) {
      document.getElementsByTagName("canvas")[0]
        ? document
            .getElementsByTagName("canvas")[0]
            .removeAttribute("class", "index_bg")
        : "";
    } else {
      document.getElementsByTagName("canvas")[0]
        ? (document.getElementsByTagName("canvas")[0].className = "index_bg")
        : "";
    }
  },
  beforeDestroy: function() {
    window.removeEventListener("resize", this.handleResize);
    document.getElementsByTagName("canvas")[0]
      ? document
          .getElementsByTagName("canvas")[0]
          .removeAttribute("class", "index_bg")
      : "";
  },
};
</script>
<style scoped lang="stylus">
.page-account {
    display: flex;
}

.page-account .code {
    display: flex;
    align-items: center;
    justify-content: center;
}

.page-account .code .pictrue {
    height: 40px;
    margin-left: 10px;
}

.swiperPross {
  width: 510px;
  height: 100%;
  border-radius: 6px 0px 0px 6px;
}

.swiperPross img {
    width: 510px;
    height: 420px;
  object-fit: cover;
}

.container {
    height: 420px !important;
    padding: 0 !important;
    /*overflow: hidden;*/
    border-radius: 6px;
    z-index: 1;
    display: flex;
}

.containerSamll {
    /*width: 56% !important;*/
    background: #fff !important;
}

.containerBig {
    width: auto !important;
    background: #f7f7f7 !important;
}

.index_from {
    padding: 0 40px 0 40px;
    box-sizing: border-box;
}

.page-account-top {
    padding: 20px 0 !important;
    box-sizing: border-box !important;
    display: flex;
    justify-content: center;
}

.page-account-container {
    border-radius: 0px 6px 6px 0px;
}

.btn {
    background: linear-gradient(90deg, rgba(25, 180, 241, 1) 0%, rgba(14, 115, 232, 1) 100%) !important;
}

.captchaBox {
    width: 310px;
}

input {
    display: block;
    width: 290px;
    line-height: 40px;
    margin: 10px 0;
    padding: 0 10px;
    outline: none;
    border: 1px solid #c8cccf;
    border-radius: 4px;
    color: #6a6f77;
}

#msg {
    width: 100%;
    line-height: 40px;
    font-size: 14px;
    text-align: center;
}

a:link, a:visited, a:hover, a:active {
    margin-left: 100px;
    color: #0366D6;
}

.index_from >>> .ivu-input-large
    font-size:14px!important
.flex{
    display: flex;
}
.login_tab{
    font-size: 16px;
    margin:0 0 20px;
    justify-content: center;
}
.login_tab_item{
    width:50%;
    text-align:center;
    padding-bottom:15px;
    border-bottom:1px solid #eee;
    cursor :pointer;
}
.active_tab{
    border-bottom:2px solid #1495ED;
    color:#1495ED;
    font-weight:600;
}
.captch_input{
    width:200px;
}
.send_code{
    flex:1;
    border:1px solid #eee;
    cursor: pointer;
    margin-left: 10px;
}
.primary{
    color:#1495ED;
    font-size:12px;
    cursor: pointer;
}
.pull-right {
  float: right!important;
  .infoUrl{
    margin 0;
    color #515a6e !important;
    &:hover{
       color #1890ff!important;
    }
  }
}
.footer{
  position: fixed;
  bottom: 0;
  width: 100%;
  left: 0;
  margin: 0;
  background: rgba(255,255,255,.8);
  border-top: 1px solid #e7eaec;
  overflow: hidden;
  padding: 10px 20px;
  height: 36px;
}
</style>
