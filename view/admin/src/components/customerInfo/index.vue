<template>
  <!-- 选择用户 -->
  <div>
    <Form
      ref="formValidate"
      inline
      :model="formValidate"
      :label-width="90"
      label-position="right"
      @submit.native.prevent
    >
      <FormItem label="注册日期：">
        <DatePicker
          :editable="false"
          @on-change="onchangeTime"
          :value="timeVal"
          format="yyyy/MM/dd"
          type="datetimerange"
          placement="bottom-end"
          placeholder="请选择时间"
          class="input-width"
          :options="options"
        ></DatePicker>
      </FormItem>
      <FormItem label="用户名称：">
        <Input
          placeholder="请输入用户名称"
          v-model="formValidate.nickname"
          class="input-add"
          @on-search="userSearchs"
        ></Input>
        <Button type="primary" @click="userSearchs()">查询</Button>
      </FormItem>
    </Form>
    <Table
      :loading="loading2"
      highlight-row
      no-userFrom-text="暂无数据"
      no-filtered-userFrom-text="暂无筛选结果"
      ref="selection"
      :columns="columns4"
      :data="tableList2"
      height="500"
    >
      <template slot-scope="{ row }" slot="headimgurl">
        <viewer>
          <div class="tabBox_img">
            <img v-lazy="row.headimgurl" />
          </div>
        </viewer>
      </template>
      <template slot-scope="{ row }" slot="user_type">
        <span v-if="row.user_type === 'wechat'">公众号</span>
        <span v-if="row.user_type === 'routine'">小程序</span>
        <span v-if="row.user_type === 'h5'">H5</span>
        <span v-if="row.user_type === 'pc'">PC</span>
      </template>
      <template slot-scope="{ row }" slot="sex">
        <span v-show="row.sex === 1">男</span>
        <span v-show="row.sex === 2">女</span>
        <span v-show="row.sex === 0">保密</span>
      </template>
      <template slot-scope="{ row }" slot="country">
        <span>{{ row.country + row.province + row.city }}</span>
      </template>
      <template slot-scope="{ row }" slot="subscribe">
        <span v-text="row.subscribe === 1 ? '关注' : '未关注'"></span>
      </template>
    </Table>
    <div class="acea-row row-right page">
      <Page
        :current="formValidate.page"
        :total="total2"
        show-elevator
        show-total
        @on-change="pageChange2"
        :page-size="formValidate.limit"
      />
    </div>
  </div>
</template>
<script>
import { kefucreateApi } from "@/api/setting";
import template from "../../pages/setting/devise/template.vue";
import timeOptions from "@/utils/timeOptions";
import { mapState } from "vuex";
export default {
  components: { template },
  name: "index",
  data() {
    return {
      formValidate: {
        page: 1,
        limit: 15,
        data: "",
        nickname: "",
      },
      tableList2: [],
      timeVal: [],
      options: timeOptions,
      fromList: {
        title: "选择时间",
        custom: true,
        fromTxt: [
          { text: "全部", val: "" },
          { text: "今天", val: "today" },
          { text: "昨天", val: "yesterday" },
          { text: "最近7天", val: "lately7" },
          { text: "最近30天", val: "lately30" },
          { text: "本月", val: "month" },
          { text: "本年", val: "year" },
        ],
      },
      currentid: 0,
      productRow: {},
      columns4: [
        {
          title: "选择",
          key: "chose",
          width: 60,
          align: "center",
          render: (h, params) => {
            let uid = params.row.uid;
            let flag = false;
            if (this.currentid === uid) {
              flag = true;
            } else {
              flag = false;
            }
            let self = this;
            return h("div", [
              h("Radio", {
                props: {
                  value: flag,
                },
                on: {
                  "on-change": () => {
                    self.currentid = uid;
                    this.productRow = params.row;
                    if (this.productRow.uid) {
                      if (this.$route.query.fodder === "image") {
                        /* eslint-disable */
                        let imageObject = {
                          image: this.productRow.headimgurl,
                          uid: this.productRow.uid,
                        };
                        form_create_helper.set("image", imageObject);
                        form_create_helper.close("image");
                      } else {
                        this.$emit("imageObject", {
                          image: this.productRow.headimgurl,
                          uid: this.productRow.uid,
                          name: this.productRow.nickname,
                        });
                      }
                    } else {
                      this.$Message.warning("请先选择商品");
                    }
                  },
                },
              }),
            ]);
          },
        },
        {
          title: "ID",
          key: "uid",
          width: 80,
        },
        {
          title: "微信用户名称",
          key: "nickname",
          minWidth: 180,
        },
        {
          title: "客服头像",
          slot: "headimgurl",
          minWidth: 60,
        },
        {
          title: "用户类型",
          slot: "user_type",
          minWidth: 100,
        },
        {
          title: "性别",
          slot: "sex",
          minWidth: 60,
        },
        {
          title: "地区",
          slot: "country",
          minWidth: 120,
        },
        {
          title: "是否关注公众号",
          slot: "subscribe",
          width: 120,
        },
      ],
      loading2: false,
      total2: 0,
    };
  },
  computed: {
    ...mapState("admin/layout", ["isMobile"]),
    labelWidth() {
      return this.isMobile ? undefined : 96;
    },
    labelPosition() {
      return this.isMobile ? "top" : "right";
    },
  },
  created() {},
  mounted() {
    this.getListService();
  },
  methods: {
    // 具体日期
    onchangeTime(e) {
      this.timeVal = e;
      this.formValidate.data = this.timeVal[0] ? this.timeVal.join("-") : "";
      this.getListService();
    },
    // 选择时间
    selectChange(tab) {
      this.formValidate.data = tab;
      this.timeVal = [];
      this.getListService();
    },
    // 客服列表
    getListService() {
      this.loading2 = true;
      kefucreateApi(this.formValidate)
        .then(async (res) => {
          let data = res.data;
          this.tableList2 = data.list;
          this.total2 = data.count;
          this.tableList2.map((item) => {
            item._isChecked = false;
          });
          this.loading2 = false;
        })
        .catch((res) => {
          this.loading2 = false;
          this.$Message.error(res.msg);
        });
    },
    pageChange2(pageIndex) {
      this.formValidate.page = pageIndex;
      this.getListService();
    },
    // 搜索
    userSearchs() {
      this.formValidate.page = 1;
      this.getListService();
    },
  },
};
</script>

<style scoped lang="stylus">
.input-add {
width: 250px;
margin-right:14px
}
.tabBox_img
    width 36px
    height 36px
    border-radius:4px;
    cursor pointer
    img
        width 100%
        height 100%
.modelBox
    >>>
    .ivu-table-header
        width 100% !important
.trees-coadd
    width: 100%;
    height: 385px;
    .scollhide
        width: 100%;
        height: 100%;
        overflow-x: hidden;
        overflow-y: scroll;
.scollhide::-webkit-scrollbar {
    display: none;
}
.footer{
    margin: 15px 0;
    padding-right: 20px;
}
</style>
