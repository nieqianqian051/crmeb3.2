<template>
<!-- 用户-客服管理-客服列表 -->
  <div>
    <Card :bordered="false" dis-hover class="ivu-mt">
      <!-- 添加客服 -->
      <Row type="flex" class="mb20">
        <Col span="24">
          <Button
            v-auth="['setting-store_service-add']"
            type="primary"
            @click="add"
            class="mr10"
            >添加客服</Button
          >
        </Col>
      </Row>
      <!-- 客服列表表格 -->
      <Table
        :columns="columns1"
        :data="tableList"
        :loading="loading"
        highlight-row
        no-userFrom-text="暂无数据"
        no-filtered-userFrom-text="暂无筛选结果"
      >
        <template slot-scope="{ row }" slot="avatar">
          <viewer>
            <div class="tabBox_img">
              <img v-lazy="row.avatar" />
            </div>
          </viewer>
        </template>
        <template slot-scope="{ row }" slot="workMember" >
          <span>{{row.workMember.name}}</span>
        </template>
        <template slot-scope="{ row }" slot="account_status">
          <i-switch
            v-model="row.account_status"
            :value="row.account_status"
            :true-value="1"
            :false-value="0"
            @on-change="onchangeIsShow(row)"
            size="large"
          >
            <span slot="open">开启</span>
            <span slot="close">关闭</span>
          </i-switch>
        </template>
        <template slot-scope="{ row }" slot="stop_time">
          <span> {{ row.stop_time | formatDate }}</span>
        </template>

        <template slot-scope="{ row, index }" slot="action">
          <a @click="edit(row)">编辑</a>
          <Divider type="vertical" />
          <a @click="del(row, '删除客服', index)">删除</a>
          <Divider type="vertical" v-if="row.account_status && row.status" />
          <a @click="goChat(row)" v-if="row.account_status && row.status"
            >进入工作台</a
          >
        </template>
      </Table>
      <div class="acea-row row-right page">
        <Page
          :total="total"
          show-elevator
          show-total
          @on-change="pageChange"
          :page-size="tableFrom.limit"
        />
      </div>
    </Card>

    <!--添加客户-->
    <!--<Modal v-model="modals" scrollable   closable title="添加客服"  width="1000"  @on-cancel="cancel">-->
    <!--<Form ref="formValidate" :model="formValidate" :label-width="labelWidth" :label-position="labelPosition" @submit.native.prevent>-->
    <!--<Row :gutter="24" type="flex">-->
    <!--<Col span="24" class="ivu-text-left">-->
    <!--<FormItem label="选择时间：">-->
    <!--<RadioGroup v-model="formValidate.data" type="button" @on-change="selectChange(formValidate.data)"-->
    <!--class="mr">-->
    <!--<Radio :label=item.val v-for="(item,i) in fromList.fromTxt" :key="i">{{item.text}}</Radio>-->
    <!--</RadioGroup>-->
    <!--<DatePicker @on-change="onchangeTime" :value="timeVal" format="yyyy/MM/dd" type="daterange"-->
    <!--placement="bottom-end" placeholder="自定义时间" style="width: 200px;"></DatePicker>-->
    <!--</FormItem>-->
    <!--</Col>-->
    <!--<Col span="12" class="ivu-text-left">-->
    <!--<FormItem label="用户名称：" >-->
    <!--<Input search enter-button  placeholder="请输入用户名称" v-model="formValidate.nickname" style="width: 90%;" @on-search="userSearchs"></Input>-->
    <!--</FormItem>-->
    <!--</Col>-->
    <!--<Col span="12" class="ivu-text-left">-->
    <!--<FormItem label="用户类型：" >-->
    <!--<Select v-model="formValidate.type" style="width:90%;" @on-change="userSearchs">-->
    <!--<Option value="">全部用户</Option>-->
    <!--<Option value="wechat">公众号</Option>-->
    <!--<Option value="routine">小程序</Option>-->
    <!--</Select>-->
    <!--</FormItem>-->
    <!--</Col>-->
    <!--</Row>-->
    <!--</Form>-->
    <!--<Table :loading="loading2" highlight-row no-userFrom-text="暂无数据" max-height="400"-->
    <!--@on-selection-change="onSelectTab"-->
    <!--no-filtered-userFrom-text="暂无筛选结果" ref="selection" :columns="columns4" :data="tableList2">-->
    <!--<template slot-scope="{ row, index }" slot="headimgurl">-->
    <!--<viewer>-->
    <!--<div class="tabBox_img">-->
    <!--<img v-lazy="row.headimgurl">-->
    <!--</div>-->
    <!--</viewer>-->
    <!--</template>-->
    <!--<template slot-scope="{ row, index }" slot="user_type">-->
    <!--<span>{{ row.user_type | typeFilter }}</span>-->
    <!--</template>-->
    <!--<template slot-scope="{ row, index }" slot="sex">-->
    <!--<span v-show="row.sex ===1">男</span>-->
    <!--<span v-show="row.sex ===2">女</span>-->
    <!--<span v-show="row.sex ===0">保密</span>-->
    <!--</template>-->
    <!--<template slot-scope="{ row, index }" slot="country">-->
    <!--<span>{{row.country + row.province + row.city}}</span>-->
    <!--</template>-->
    <!--<template slot-scope="{ row, index }" slot="subscribe">-->
    <!--<span v-text="row.subscribe === 1?'关注':'未关注'"></span>-->
    <!--</template>-->
    <!--</Table>-->
    <!--<div class="acea-row row-right page">-->
    <!--<Page :total="total2" :current="formValidate.page" show-elevator show-total @on-change="pageChange2"-->
    <!--:page-size="formValidate.limit"/>-->
    <!--</div>-->
    <!--<div slot="footer">-->
    <!--<Button  type="primary"  @click="putRemark">提交</Button>-->
    <!--</div>-->
    <!--</Modal>-->

    <!--聊天记录-->
    <Modal
      v-model="modals3"
      footer-hide
      scrollable
      closable
      title="聊天记录"
      width="700"
    >
      <div v-if="isChat" class="modelBox">
        <Table
          :loading="loading3"
          highlight-row
          no-userFrom-text="暂无数据"
          no-filtered-userFrom-text="暂无筛选结果"
          :columns="columns3"
          :data="tableList3"
        >
          <template slot-scope="{ row }" slot="headimgurl">
            <viewer>
              <div class="tabBox_img">
                <img v-lazy="row.headimgurl" />
              </div>
            </viewer>
          </template>
          <template slot-scope="{ row }" slot="action">
            <a @click="look(row)">查看对话</a>
          </template>
        </Table>
        <div class="acea-row row-right page">
          <Page
            :total="total3"
            show-elevator
            show-total
            @on-change="pageChange3"
            :page-size="formValidate3.limit"
          />
        </div>
      </div>
      <div v-if="!isChat">
        <Button type="primary" @click="isChat = true">返回聊天记录</Button>
        <Table
          :loading="loading5"
          highlight-row
          no-userFrom-text="暂无数据"
          class="mt20"
          no-filtered-userFrom-text="暂无筛选结果"
          :columns="columns5"
          :data="tableList5"
        >
          <template slot-scope="{ row }" slot="avatar">
            <viewer>
              <div class="tabBox_img">
                <img v-lazy="row.avatar" />
              </div>
            </viewer>
          </template>
          <template slot-scope="{ row }" slot="action">
            <a @click="look(row)">查看对话</a>
          </template>
        </Table>
        <div class="acea-row row-right page">
          <Page
            :total="total5"
            show-elevator
            show-total
            @on-change="pageChange5"
            :page-size="formValidate5.limit"
          />
        </div>
      </div>
    </Modal>
  </div>
</template>

<script>
import { mapState } from "vuex";
import util from "@/libs/util";
import {
  kefuListApi,
  kefucreateApi,
  kefuaddApi,
  kefuAddApi,
  kefusetStatusApi,
  kefuEditApi,
  kefuRecordApi,
  kefuChatlistApi,
  kefuLogin,
} from "@/api/setting";
export default {
  name: "index",
  filters: {
    typeFilter(status) {
      const statusMap = {
        wechat: "微信用户",
        routine: "小程序用户",
      };
      return statusMap[status];
    },
  },
  computed: {
    ...mapState("admin/layout", ["isMobile"]),
    ...mapState("admin/userLevel", ["categoryId"]),
    labelWidth() {
      return this.isMobile ? undefined : 80;
    },
    labelPosition() {
      return this.isMobile ? "top" : "left";
    },
  },
  data() {
    return {
      isChat: true,
      formValidate3: {
        page: 1,
        limit: 15,
      },
      total3: 0,
      loading3: false,
      modals3: false,
      tableList3: [],
      columns3: [
        {
          title: "用户名称",
          key: "nickname",
          width: 200,
        },
        {
          title: "客服头像",
          slot: "headimgurl",
        },
        {
          title: "操作",
          slot: "action",
        },
      ],
      formValidate5: {
        page: 1,
        limit: 15,
        uid: 0,
        to_uid: 0,
        id: 0,
      },
      total5: 0,
      loading5: false,
      tableList5: [],
      columns5: [
        {
          title: "用户名称",
          key: "nickname",
          width: 200,
        },
        {
          title: "用户头像",
          slot: "avatar",
        },
        {
          title: "发送消息",
          key: "msn",
          width: 250,
        },
        {
          title: "发送时间",
          key: "add_time",
        },
      ],
      FromData: null,
      formValidate: {
        page: 1,
        limit: 15,
        data: "",
        type: "",
        nickname: "",
      },
      tableList2: [],
      modals: false,
      total: 0,
      tableFrom: {
        page: 1,
        limit: 15,
      },
      timeVal: [],
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
      loading: false,
      tableList: [],
      columns1: [
        {
          title: "ID",
          key: "id",
          width: 80,
        },
        {
          title: "微信用户名称",
          key: "nickname",
          minWidth: 120,
        },
        {
          title: "客服头像",
          slot: "avatar",
          minWidth: 60,
        },
        {
          title: "客服名称",
          key: "wx_name",
          minWidth: 120,
        },
        {
          title: "企微员工",
          slot: "workMember",
          minWidth: 120,
        },
        {
          title: "账号状态",
          slot: "account_status",
          minWidth: 120,
        },
        {
          title: "添加时间",
          key: "add_time",
          minWidth: 130,
        },
        {
          title: "操作",
          slot: "action",
          fixed: "right",
          width: 200,
        },
      ],
      columns4: [
        {
          type: "selection",
          width: 60,
          align: "center",
        },
        {
          title: "ID",
          key: "uid",
          width: 80,
        },
        {
          title: "微信用户名称",
          key: "nickname",
          minWidth: 160,
        },
        {
          title: "客服头像",
          slot: "headimgurl",
          minWidth: 60,
        },
        {
          title: "用户类型",
          slot: "user_type",
          width: 100,
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
          minWidth: 120,
        },
      ],
      loading2: false,
      total2: 0,
      addFrom: {
        uids: [],
      },
      selections: [],
      rows: {},
      rowRecord: {},
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 进入工作台
    goChat(item) {
      kefuLogin(item.id)
        .then((res) => {
          var url = "";
          if (res.data.token) {
            let expires = this.getExpiresTime(res.data.exp_time);
            util.cookies.setKefu("token", res.data.token, expires);
            util.cookies.setKefu("uuid", res.data.kefuInfo.uid, expires);
            util.cookies.setKefu("expires_time", res.data.exp_time, expires);
            util.cookies.setKefu("kefuInfo", res.data.kefuInfo, expires);
            if (this.$store.state.admin.layout.isMobile) {
              url =
                window.location.protocol +
                "//" +
                window.location.host +
                "/kefu/mobile_list";
            } else {
              url =
                window.location.protocol +
                "//" +
                window.location.host +
                "/kefu/pc_list";
            }
            window.open(url, "_blank");
          }
        })
        .catch((error) => {
          this.$Message.error(error.msg);
        });
    },
    getExpiresTime(expiresTime) {
      let nowTimeNum = Math.round(new Date() / 1000);
      let expiresTimeNum = expiresTime - nowTimeNum;
      return parseFloat(parseFloat(parseFloat(expiresTimeNum / 60) / 60) / 24);
    },
    cancel() {
      this.formValidate = {
        page: 1,
        limit: 10,
        data: "",
        type: "",
        nickname: "",
      };
    },
    handleReachBottom() {
      return new Promise((resolve) => {
        this.formValidate.page = this.formValidate.page + 1;
        setTimeout(() => {
          // this.loading2 = true;
          kefucreateApi(this.formValidate)
            .then(async (res) => {
              let data = res.data;
              // this.tableList2 = data.list;
              if (data.list.length > 0) {
                for (let i = 0; i < data.list.length; i++) {
                  this.tableList2.push(data.list[i]);
                }
              }
              this.total2 = data.count;
              this.loading2 = false;
            })
            .catch((res) => {
              this.loading2 = false;
              this.$Message.error(res.msg);
            });
          resolve();
        }, 2000);
      });
    },
    // 查看对话
    look(row) {
      this.isChat = false;
      this.rowRecord = row;
      this.getChatlist();
    },
    // 查看对话列表
    getChatlist() {
      this.loading5 = true;
      this.formValidate5.uid = this.rows.uid;
      this.formValidate5.to_uid = this.rowRecord.uid;
      this.formValidate5.id = this.rows.id;
      kefuChatlistApi(this.formValidate5)
        .then(async (res) => {
          let data = res.data;
          this.tableList5 = data.list;
          this.total5 = data.count;
          this.loading5 = false;
        })
        .catch((res) => {
          this.loading5 = false;
          this.$Message.error(res.msg);
        });
    },
    pageChange5(index) {
      this.formValidate5.page = index;
      this.getChatlist();
    },
    // 修改成功
    submitFail() {
      this.getList();
    },
    // 聊天记录
    record(row) {
      this.rows = row;
      this.modals3 = true;
      this.isChat = true;
      this.getListRecord();
    },
    // 聊天记录列表
    getListRecord() {
      this.loading3 = true;
      kefuRecordApi(this.formValidate3, this.rows.id)
        .then(async (res) => {
          let data = res.data;
          this.tableList3 = data.list ? data.list : [];
          this.total3 = data.count;
          this.loading3 = false;
        })
        .catch((res) => {
          this.loading3 = false;
          this.$Message.error(res.msg);
        });
    },
    pageChange3(index) {
      this.formValidate3.page = index;
      this.getListRecord();
    },
    // 编辑
    edit(row) {
      this.$modalForm(kefuEditApi(row.id)).then(() => this.getList());
    },
    // 添加
    add() {
      // this.modals = true;
      // this.formValidate.data = '';
      // this.getListService();
      this.$modalForm(kefuaddApi()).then(() => this.getList());
    },
    // 全选
    onSelectTab(selection) {
      this.selections = selection;
      let data = [];
      this.selections.map((item) => {
        data.push(item.uid);
      });
      this.addFrom.uids = data;
    },
    // 具体日期
    onchangeTime(e) {
      this.timeVal = e;
      this.formValidate.data = this.timeVal[0] ? this.timeVal.join("-") : "";
      this.formValidate.page = 1;
      this.getListService();
    },
    // 选择时间
    selectChange(tab) {
      this.formValidate.data = tab;
      this.timeVal = [];
      this.formValidate.page = 1;
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
      this.addFrom.uids = [];
    },
    // 搜索
    userSearchs() {
      this.formValidate.page = 1;
      this.getListService();
    },
    // 删除
    del(row, tit, num) {
      let delfromData = {
        title: tit,
        num: num,
        url: `app/wechat/kefu/${row.id}`,
        method: "DELETE",
        ids: "",
      };
      this.$modalSure(delfromData)
        .then((res) => {
          this.$Message.success(res.msg);
          this.tableList.splice(num, 1);
          if (!this.tableList.length) {
            this.tableFrom.page =
                this.tableFrom.page == 1 ? 1 : this.tableFrom.page - 1;
          }
          this.getList();
        })
        .catch((res) => {
          this.$Message.error(res.msg);
        });
    },
    // 列表
    getList() {
      this.loading = true;
      kefuListApi(this.tableFrom)
        .then(async (res) => {
          let data = res.data;
          this.tableList = data.list;
          this.total = res.data.count;
          this.loading = false;
        })
        .catch((res) => {
          this.loading = false;
          this.$Message.error(res.msg);
        });
    },
    pageChange(index) {
      this.tableFrom.page = index;
      this.getList();
    },
    // 修改是否显示
    onchangeIsShow(row) {
      let data = {
        id: row.id,
        account_status: row.account_status,
      };
      kefusetStatusApi(data)
        .then(async (res) => {
          this.$Message.success(res.msg);
          this.getList();
        })
        .catch((res) => {
          this.$Message.error(res.msg);
        });
    },
    // 添加客服
    putRemark() {
      if (this.addFrom.uids.length === 0) {
        return this.$Message.warning("请选择要添加的客服");
      }
      kefuAddApi(this.addFrom)
        .then(async (res) => {
          this.$Message.success(res.msg);
          this.modals = false;
          this.getList();
        })
        .catch((res) => {
          this.loading = false;
          this.$Message.error(res.msg);
        });
    },
    // 进入工作台
    // goChat(item){
    //     kefuLogin(item.id).then(res=>{
    //         if(res.data.token){
    //             let expires = this.getExpiresTime(res.data.exp_time);
    //             util.cookies.setKefu('token', res.data.token, {
    //                 expires:expires
    //             });
    //             util.cookies.setKefu('uuid', res.data.kefuInfo.uid, {
    //                 expires: expires
    //             });
    //             util.cookies.setKefu('expires_time', res.data.exp_time, {
    //                 expires: expires
    //             });
    //             let url = window.location.protocol+"//"+window.location.host + '/kefu';
    //             window.open(url, '_blank');
    //         }
    //     })
    // },
    // getExpiresTime (expiresTime) {
    //     let nowTimeNum = Math.round(new Date() / 1000);
    //     let expiresTimeNum = expiresTime - nowTimeNum;
    //     return parseFloat(parseFloat(parseFloat(expiresTimeNum / 60) / 60) / 24);
    // },
  },
};
</script>

<style scoped lang="stylus">
.tabBox_img {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  cursor: pointer;

  img {
    width: 100%;
    height: 100%;
  }
}

.modelBox {
  >>>, .ivu-table-header {
    width: 100% !important;
  }
}

.trees-coadd {
  width: 100%;
  height: 385px;

  .scollhide {
    width: 100%;
    height: 100%;
    overflow-x: hidden;
    overflow-y: scroll;
  }
}

// margin-left: 18px;
.scollhide::-webkit-scrollbar {
  display: none;
}
</style>
