<template>
<!-- 设置-小程序 -->
  <div>
    <Card :bordered="false" dis-hover class="ivu-mt">
      <Button slot="extra" type="text" custom-icon="iconfont iconpeizhiyindao1" @click="showGuide">配置引导</Button>
      <div class="flex-wrapper">
        <!-- :src="iframeUrl" -->
        <div>
          <iframe
            class="iframe-box"
            :src="iframeUrl"
            frameborder="0"
            ref="iframe"
          ></iframe>
          <div class="mask"></div>
        </div>

        <div class="right">
          <div class="content">
            <div class="content-box title">
              <div class="line"></div>
              <div class="right title">基础配置</div>
            </div>
            <div class="content-box">
              <div class="left">appid：</div>
              <div class="right">
                <Input v-model="formData.routine_appId" placeholder="请输入appid" />
              </div>
            </div>
            <div class="content-box">
              <div class="left">AppSecret：</div>
              <div class="right">
                <Input v-model="formData.routine_appsecret" placeholder="请输入AppSecret" />
              </div>
            </div>
            <div class="content-box">
              <div class="left">小程序名称：</div>
              <div class="right">
                <Input v-model="formData.routine_name" placeholder="请输入小程序名称" />
              </div>
            </div>
            <div class="content-box">
              <div class="left">小程序客服类型：</div>
              <div class="right">
                <RadioGroup v-model="formData.routine_contact_type">
                  <Radio :label="1">
                    <span>小程序客服</span>
                  </Radio>
                  <Radio :label="0">
                    <span>内置客服系统</span>
                  </Radio>
                </RadioGroup>
              </div>
            </div>
            <div class="content-box">
              <div class="left">强制获取昵称头像：</div>
              <div class="right">
                <RadioGroup v-model="formData.store_user_avatar">
                  <Radio :label="0">
                    <span>关闭</span>
                  </Radio>
                  <Radio :label="1">
                    <span>开启</span>
                  </Radio>
                </RadioGroup>
              </div>
            </div>
            <div class="content-box last">
              <div class="left"></div>
              <div class="right">
                <div class="tip">{{avatarDesc}}</div>
              </div>
            </div>
            <div class="content-box">
              <div class="left">发货信息管理：</div>
              <div class="right">
                <RadioGroup v-model="formData.order_shipping_open">
                  <Radio :label="0">
                    <span>关闭</span>
                  </Radio>
                  <Radio :label="1">
                    <span>开启</span>
                  </Radio>
                </RadioGroup>
              </div>
            </div>
            <div class="content-box">
              <div class="left">手机号获取方式：</div>
              <div class="right">
                <CheckboxGroup v-model="formData.routine_auth_type">
                  <Checkbox :label="1">
                    <span>微信授权</span>
                  </Checkbox>
                  <Checkbox :label="2">
                    <span>手机号登录</span>
                  </Checkbox>
                </CheckboxGroup>
              </div>
            </div>
            <div class="content-box last">
              <div class="left"></div>
              <div class="right">
                <div class="tip">{{shippingDesc}}</div>
              </div>
            </div>
          </div>
          <div class="content" style="margin-bottom: 60px;">
            <div class="content-box title">
              <div class="line"></div>
              <div class="right title">相关下载</div>
            </div>
            <Alert v-if="!pageData.appId && !pageData.code" closable>
              <template slot="desc">
                您尚未配置小程序信息，请<router-link
                  :to="{ path: '/admin/setting/system_config?from=download' }"
                  >立即设置</router-link
                ></template
              >
            </Alert>
           <!-- <div class="content-box">
              <div class="left">小程序标题：</div>
              <div class="right">{{ pageData.routine_name || "未命名" }}</div>
            </div> -->
            <div class="content-box">
              <div class="left">小程序码：</div>
              <div class="right">
                <Button type="primary" @click="downLoadCode(pageData.code)"
                  >下载小程序码</Button
                >
              </div>
            </div>
            <div class="content-box">
              <div class="left">小程序源码包：</div>
              <div class="right">
                <!-- <span>是否已开通小程序直播</span> -->
                <RadioGroup size="large" v-model="is_live">
                  <Radio :label="0">未开通直播</Radio>
                  <Radio :label="1">已开通直播</Radio>
                </RadioGroup>
              </div>
            </div>
            <div class="content-box last">
              <div class="left"></div>
              <div class="right">
                <div class="tip">
                  请谨慎选择是否有开通小程序直播功能，否则将影响小程序的发布
                  可前往
                  <a :href="pageData.help" target="_blank">帮助文档</a>
                  查看如何开通直播功能
                </div>

                <Button class="mt10" type="primary" @click="downLoad()"
                  >源码包下载</Button
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </Card>
    <Card :bordered="false" dis-hover class="submit-card">
      <Button type="primary" @click="submitForm">提交</Button>
    </Card>
    <Drawer v-model="guideShow" title="小程序配置引导" width="800">
      <routine></routine>
    </Drawer>
  </div>
</template>

<script>
import routine from '@/components/settingGuide/routine'
import { routineDownload, routineInfo } from "@/api/app";
import { saveBasicsApi, getNewFormBuildRuleApi } from "@/api/setting";
import { mapState } from "vuex";
export default {
  name: "routineTemplate",
  components: { routine },
  data() {
    return {
      grid: {
        xl: 7,
        lg: 7,
        md: 12,
        sm: 24,
        xs: 24,
      },
      iframeUrl: `${location.origin}/pages/index/index`,
      is_live: 1,
      pageData: {
        code: "",
        routine_name: "",
        help: "",
        appId: "1",
      },
      formData: {
        routine_appId: '',
        routine_appsecret: '',
        routine_name: '',
        store_user_avatar: 0,
        order_shipping_open: 0,
        routine_contact_type: 1,
        routine_auth_type:[1,2]
      },
      guideShow: false,
      avatarDesc: '',
      shippingDesc: '',
    };
  },
  created() {
    routineInfo().then((res) => {
      this.pageData = res.data;
    });
    getNewFormBuildRuleApi('routine').then(res => {
        let data = res.data;
        this.formData.routine_appId = data.routine_appId.value;
        this.formData.routine_appsecret = data.routine_appsecret.value;
        this.formData.routine_name = data.routine_name.value;
        this.formData.routine_contact_type = data.routine_contact_type.value;
        this.formData.store_user_avatar = data.store_user_avatar.value;
        this.avatarDesc = data.store_user_avatar.desc;
        this.shippingDesc = data.order_shipping_open.desc;
        this.formData.order_shipping_open = data.order_shipping_open.value;
        this.formData.routine_auth_type = data.routine_auth_type.value; //
    })
  },
  watch: {
    $route(to, from) {},
  },
  computed: {
    ...mapState("media", ["isMobile"]),
    labelWidth() {
      return this.isMobile ? undefined : 80;
    },
    labelPosition() {
      return this.isMobile ? "top" : "left";
    },
  },
  methods: {
    downLoad() {
      routineDownload({
        is_live: this.is_live,
      })
        .then((res) => {
          window.open(res.data.url);
        })
        .catch((err) => {
          this.$Message.warning(err.msg);
        });
    },
    downLoadCode(url) {
      if (!url) return this.$Message.warning("暂无小程序码");
      var image = new Image();
      image.src = url;
      // 解决跨域 Canvas 污染问题
      image.setAttribute("crossOrigin", "anonymous");
      image.onload = function () {
        var canvas = document.createElement("canvas");
        canvas.width = image.width;
        canvas.height = image.height;
        var context = canvas.getContext("2d");
        context.drawImage(image, 0, 0, image.width, image.height);
        var url = canvas.toDataURL(); //得到图片的base64编码数据
        var a = document.createElement("a"); // 生成一个a元素
        var event = new MouseEvent("click"); // 创建一个单击事件
        a.download = name || "photo"; // 设置图片名称
        a.href = url; // 将生成的URL设置为a.href属性
        a.dispatchEvent(event); // 触发a的单击事件
      };
    },
    submitForm () {
      saveBasicsApi(this.formData).then(res => {
        this.$Message.success(res.msg);
      }).catch(res => {
        this.$Message.error(res.msg);
      });
    },
    showGuide() {
      this.guideShow = true;
    }
  },
};
</script>

<style scoped lang="stylus">
/deep/.ivu-btn > .ivu-icon + span{
  margin-left: 0;
}
/deep/.ivu-btn > i{
  vertical-align: -1px;
}
.ivu-btn-text {
  color: #2D8CF0;
  font-size: 13px !important;
}
.template_sp_box {
  padding: 5px 0;
  box-sizing: border-box;
}

.template_sp {
  display: block;
  padding: 2px 0;
  box-sizing: border-box;
}

.flex-wrapper {
  display: flex;
  border-radius: 10px;
}

.iframe-box {
  width: 294px;
  height: 523px;
  border-radius: 10px;
  margin-top: 14px;
  margin-left: 24px;
}

.ivu-mt {
  // height: 600px;
}

.content {
  padding: 0 0 0 60px;
  margin-top: 60px;

  &:first-child {
    margin-top: 40px;
  }

  .ivu-radio-wrapper {
    margin-right: 30px;
  }
}

.content > .title {
  padding-bottom: 30px;
  margin-bottom: 0;
  font-size: 15px;
  line-height: 15px;
  color: #000000;
}

.content-box {
  display: flex;
  align-items: center;
  margin-bottom: 26px;
  color: #333;

  .tip {
    width: 348px;
    margin-top: -12px;
    font-size: 12px;
    line-height: 17px;
    color: #999999;
  }

  &.last {
    .ivu-btn-primary {
      margin-top: 14px;
    }
  }
}

.content-box:last-child {
  margin-bottom: 0;
}

.content-box.last {
  margin-top: 0;
  color: #999999;
}

.line {
  width: 3px;
  height: 16px;
  background-color: #1890FF;
  margin-right: 11px;
}

.content-box .title {
  font-size: 16px;
  font-weight: bold;
}

.content-box > span {
  color: #F5222D;
  font-size: 20px;
}

.content-box .left {
  width: 130px;
  text-align: right;
}

.content-box .right {
  width: 460px;
}

.rad {
  margin-left: 20px;
}

.mask {
  position: absolute;
  left: 0;
  top: 0;
  width: 312px;
  height: 550px;
  background-color: rgba(0, 0, 0, 0);
}

.submit-card {
  margin-top: 14px;
  text-align: center;

  .ivu-btn {
    padding: 0 20px;
  }
}

.ivu-btn-text {
  color: #2D8CF0;
}

.ivu-btn-text:focus {
  box-shadow: none;
}

/deep/.ivu-drawer-wrap {
  .ivu-tabs-bar {
    margin-bottom: 30px;
  }

  .ivu-tabs-nav {
    display: flex;
    width: 100%;
  }

  .ivu-tabs-ink-bar {
    display: none;
  }

  .ivu-tabs-tab {
    flex: 1;
    padding: 10px 16px 10px 32px;
    margin-right: 0;
    background-color: #F5F5F5;
    text-align: center;
    font-size: 15px;
    line-height: 21px;
    color: #666666;
    transition: none;

    &::before {
      content: '';
      position: absolute;
      top: 6px;
      left: -14px;
      width: 29px;
      height: 29px;
      border: inherit;
      border-left-color: transparent;
      border-bottom-color: transparent;
      background-color: #FFFFFF;
      transform: rotate(45deg);
    }

    &::after {
      content: '';
      position: absolute;
      top: 6px;
      right: -14px;
      z-index: 3;
      width: 29px;
      height: 29px;
      border: inherit;
      border-left-color: transparent;
      border-bottom-color: transparent;
      background: inherit;
      transform: rotate(45deg);
    }

    &:hover {
      color: #666666 !important;
    }
  }

  .ivu-tabs-tab-active {
    background-color: #2D8BEF;
    color: #FFFFFF;

    &:hover {
      color: #FFFFFF !important;
    }
  }

  .ivu-timeline {
    .ivu-timeline-item-tail {
      left: 10px;
    }

    .ivu-timeline-item-head-custom {
      left: 0;
      padding: 0;
      margin-top: 0;
    }

    .ivu-timeline-item-content {
      top: -10px;
      padding: 0 0 10px 30px;
    }

    .dot {
      width: 20px;
      height: 20px;
      border-radius: 50%;
      background-color: #2D8CF0;
      line-height: 20px;
      color: #FFFFFF;
    }

    .title {
      margin-bottom: 10px;
      font-size: 16px;
      line-height: 20px;
      color: #333333;
    }

    .item + .item {
      margin-top: 20px;
    }

    .text {
      font-size: 12px;
      line-height: 17px;
      color: #666666;
    }

    .image {
      margin-top: 8px;
    }

    img {
      display: block;
      width: 320px;
      height: 160px;

      + img {
        margin-top: 8px;
      }
    }

    .ivu-alert {
      margin-top: 8px;
      color: #666666;

      div span {
        color: #2D8CF0;
      }
    }
  }
}
</style>
