<template>
  <div>
    <Card :bordered="false" dis-hover class="mt15">
      <Form
          ref="orderData"
          :model="orderData"
          :label-width="labelWidth"
          :label-position="labelPosition"
          class="tabform"
          @submit.native.prevent
      >
        <div class="acea-row">
          <FormItem label="时间选择：">
            <DatePicker
                :editable="false"
                :clearable="true"
                @on-change="onchangeTime"
                :value="timeVal"
                format="yyyy/MM/dd HH:mm:ss"
                type="datetimerange"
                placement="bottom-start"
                placeholder="自定义时间"
                style="width: 250px"
                class="mr30"
                :options="options"
            ></DatePicker>
          </FormItem>
          <FormItem label="活动类型：">
            <Select
                v-model="orderData.type"
                style="width: 250px"
                class="mr30"
                clearable
                @on-change="userSearchs"
                placeholder="全部订单"
            >
              <Option value="" >全部订单</Option>
              <Option value="0">普通订单</Option>
              <Option value="1">秒杀订单</Option>
              <Option value="2">砍价订单</Option>
              <Option value="3">拼团订单</Option>
              <Option value="4">积分订单</Option>
              <Option value="5">套餐订单</Option>
              <Option value="6">预售商品</Option>
              <Option value="7">新人订单</Option>
              <Option value="8">抽奖订单</Option>
            </Select>
          </FormItem>
          <FormItem label="订单状态：">
            <Select
                v-model="orderData.status"
                style="width: 250px"
                class="mr30"
                clearable
                @on-change="userSearchs"
                placeholder="全部"
            >
              <Option value="" >全部</Option>
              <Option value="0">未支付</Option>
              <Option value="1">未发货</Option>
              <Option value="2">待收货</Option>
              <Option value="3">待评价</Option>
              <Option value="4">交易完成</Option>
              <Option value="-4">已删除</Option>
            </Select>
          </FormItem>
          <FormItem label="支付方式：">
            <Select v-model="orderData.pay_type" clearable class="mr30" style="width: 250px" @on-change="userSearchs" placeholder="全部" >
              <Option v-for="item in payList" :value="item.val" :key="item.id">{{
                  item.label
                }}</Option>
            </Select>
          </FormItem>
          <FormItem label="订单搜索：">
            <Input
                v-model="orderData.real_name"
                placeholder="请输入"
                element-id="name"
                clearable
                style="width:250px;"
                maxlength="20"
            >
              <Select
                  v-model="orderData.field_key"
                  slot="prepend"
                  style="width:80px;"
                  default-label="全部"
              >
                <Option value="all">全部</Option>
                <Option value="order_id">订单号</Option>
                <Option value="uid">用户UID</Option>
                <Option value="real_name">用户姓名</Option>
                <Option value="user_phone">用户电话</Option>
                <Option value="title">商品名称</Option>
                <Option value="total_num">商品件数</Option>
              </Select>
            </Input>
          </FormItem>
          <Button type="primary" @click="userSearchs" class="ml20">查询</Button>
          <Button @click="reset" class="ml20">重置</Button>
        </div>
      </Form>
    </Card>
    <Card :bordered="false" dis-hover class="mt15">
      <div class="acea-row row-between">
        <div>
          <Tooltip content="本页至少选中一项" :disabled="!!checkUidList.length && isAll==0">
            <Button class="mr10" type="primary" :disabled="!checkUidList.length && isAll==0" @click="delAll">批量删除订单</Button>
          </Tooltip>
          <Button class="mr10" type="primary" @click="manualModal = true">手动批量发货</Button>
          <Tooltip content="本页至少选中一项" :disabled="!!checkUidList.length && isAll==0">
            <Button class="mr10" type="primary" :disabled="!checkUidList.length && isAll==0" @click="printOreder">打印配货单</Button>
          </Tooltip>
          <Dropdown
              class="mr10"
              @on-click="exports"
          >
            <Button style="width: 110px">
              {{ exportList[exportListOn].label }}
              <Icon type="ios-arrow-down"></Icon>
            </Button>
            <DropdownMenu slot="list">
              <DropdownItem
                  v-for="(item, index) in exportList"
                  :key="index"
                  :name="item.name"
                  style="font-size: 12px !important"
              >{{ item.label }}</DropdownItem
              >
            </DropdownMenu>
          </Dropdown>
          <!--<Button class="mr10 greens" size="default" @click="writeOff">订单核销</Button>-->
        </div>
        <div>
          <Button class="mr10" @click="queuemModal">批量发货记录</Button>
          <Button class="mr10" @click="getExpressList">下载物流公司对照表</Button>
        </div>
      </div>
      <vxe-table
          border="inner"
          ref="xTable"
          class="mt25"
          :loading="loading"
          row-id="id"
          :expand-config="{accordion: true}"
          :checkbox-config="{reserve: true}"
          @checkbox-all="checkboxAll"
          @checkbox-change="checkboxItem"
          :data="orderList">
        <vxe-column type="" width="0"></vxe-column>
        <vxe-column type="expand" width="35">
          <template #content="{ row }">
            <div class="tdinfo">
              <Row class="expand-row">
                <Col span="8">
                  <span class="expand-key">商品总价：</span>
                  <span class="expand-value" v-text="row.total_price"></span>
                </Col>
                <Col span="8">
                  <span class="expand-key">下单时间：</span>
                  <span class="expand-value" v-text="row.add_time"></span>
                </Col>
                <Col span="8">
                  <span class="expand-key">推广人：</span>
                  <span class="expand-value" v-text="row.spread_nickname?row.spread_nickname:'无'"></span>
                </Col>
              </Row>
              <Row class="expand-row">
                <Col span="8">
                  <span class="expand-key">用户备注：</span>
                  <span class="expand-value" v-text="row.mark?row.mark:'无'"></span>
                </Col>
                <Col span="8">
                  <span class="expand-key">商家备注：</span>
                  <span class="expand-value" v-text="row.remark?row.remark:'无'"></span>
                </Col>
              </Row>
            </div>
          </template>
        </vxe-column>
        <vxe-column type="checkbox" width="100">
          <template #header>
            <div>
              <Dropdown transfer @on-click="allPages">
                <a href="javascript:void(0)" class="acea-row row-middle">
                  <span>全选({{isAll==1?(total-checkUidList.length):checkUidList.length}})</span>
                  <Icon type="ios-arrow-down"></Icon>
                </a>
                <template #list>
                  <DropdownMenu>
                    <DropdownItem name="0">当前页</DropdownItem>
                    <DropdownItem name="1">所有页</DropdownItem>
                  </DropdownMenu>
                </template>
              </Dropdown>
            </div>
          </template>
        </vxe-column>
        <vxe-column field="order_id" title="订单号" min-width="190">
          <template v-slot="{ row }">
            <Tooltip theme="dark" max-width="300" :delay="600" content="用户已删除" v-if="row.is_del === 1 && row.delete_time == null">
              <span style="color: #ed4014; display: block">{{row.order_id}}</span>
            </Tooltip>
            <span @click="changeMenu(row,'2')" v-else style="color: #2D8cF0; display: block;cursor: pointer;">{{row.order_id}}</span>
          </template>
        </vxe-column>
        <vxe-column field="info" title="商品信息" min-width="330">
          <template v-slot="{ row }">
            <Tooltip :transfer="true" theme="dark" max-width="300" :delay="600">
              <div class="tabBox" v-for="(val, i) in row._info" :key="i">
                <div class="tabBox_img" v-viewer>
                  <img
                      v-lazy="
					        val.cart_info.productInfo.attrInfo
					          ? val.cart_info.productInfo.attrInfo.image
					          : val.cart_info.productInfo.image
					      "
                  />
                </div>
                <span class="tabBox_tit line1">
					    <span class="font-color-red" v-if="val.cart_info.is_gift">赠品</span>
					    {{ val.cart_info.productInfo.store_name + " | "}}
					    {{val.cart_info.productInfo.attrInfo ? val.cart_info.productInfo.attrInfo.suk: ""}}
					  </span>
              </div>
              <div slot="content">
                <div v-for="(val, i) in row._info" :key="i">
                  <p class="font-color-red" v-if="val.cart_info.is_gift">赠品</p>
                  <p>{{ val.cart_info.productInfo.store_name }}</p>
                  <p> {{val.cart_info.productInfo.attrInfo ? val.cart_info.productInfo.attrInfo.suk: ""}}</p>
                  <p class="tabBox_pice">{{ "￥" + val.cart_info.sum_price + " x " + val.cart_info.cart_num }}</p>
                </div>
              </div>
            </Tooltip>
          </template>
        </vxe-column>
        <vxe-column field="nickname" title="用户信息" min-width="130">
          <template v-slot="{ row }">
            <a @click="showUserInfo(row)">{{ row.nickname }}</a>
            <span style="color: #ed4014;" v-if="row.delete_time != null"> (已注销)</span>
          </template>
        </vxe-column>
        <vxe-column field="pink_name" title="活动类型" min-width="110"></vxe-column>
        <vxe-column field="pay_price" title="实际支付" min-width="90">
          <template v-slot="{ row }">
            <div>{{row.paid>0?row.pay_price:0}}</div>
          </template>
        </vxe-column>
        <vxe-column field="pay_type_name" title="支付方式" min-width="110"></vxe-column>
        <vxe-column field="_pay_time" title="支付时间" min-width="130"></vxe-column>
        <vxe-column field="statusName" title="订单状态" min-width="100">
          <template v-slot="{ row }">
            <Tag color="default" size="medium" v-show="row.status == 3">{{ row.status_name.status_name }}</Tag>
            <Tag color="orange" size="medium" v-show="row.status == 4">{{row.status_name.status_name }}</Tag>
            <Tag color="orange"  size="medium"  v-show="[1,2,5].includes(row.status)">{{ row.status_name.status_name }}</Tag>
            <Tag color="red" size="medium" v-show="row.status == 0">{{ row.status_name.status_name }}</Tag>
            <Tag color="orange" size="medium" v-if="!row.is_all_refund && row.refund.length">部分退款中</Tag>
            <Tag color="orange" size="medium"  v-if="row.is_all_refund && row.refund.length && row.refund_type != 6">退款中</Tag>
            <div class="pictrue-box" size="medium"  v-if="row.status_name.pics">
              <div
                  v-viewer
                  v-for="(item, index) in row.status_name.pics || []"
                  :key="index"
              >
                <img class="pictrue mr10" v-lazy="item" :src="item" />
              </div>
            </div>
          </template>
        </vxe-column>
        <vxe-column field="pay_type" title="支付状态" min-width="90">
          <template v-slot="{ row }">
            <div>{{row.paid==1?'已支付':'未支付'}}</div>
          </template>
        </vxe-column>
        <vxe-column field="action" title="操作" align="center" width="130" fixed="right">
          <template v-slot="{ row }">
            <a
                @click="sendOrder(row)"
                v-if="
					  (row._status === 2 || row._status === 8 || row.status === 4) &&
					  row.shipping_type === 1 &&
					  (row.pinkStatus === null || row.pinkStatus === 2) && row.delete_time == null
					"
            >发送货</a
            >
            <Divider type="vertical" v-if="(row._status === 2 || row._status === 8 || row.status === 4) &&
					  row.shipping_type === 1 &&
					  (row.pinkStatus === null || row.pinkStatus === 2) && row.delete_time == null" />
            <a @click="changeMenu(row,'2')">详情</a>
          </template>
        </vxe-column>
      </vxe-table>
      <div class="acea-row row-right mt15">
        <Page :total="total" :current="orderData.page" show-elevator show-total @on-change="pageChange"
              :page-size="orderData.limit"/>
      </div>
    </Card>
    <!-- 编辑 配送信息表单数据 退款 退积分 不退款-->
    <edit-from
        ref="edits"
        :FromData="FromData"
        @submitFail="submitFail"
    ></edit-from>
    <!-- 详情 -->
    <details-from
        ref="detailss"
        :orderDatalist="orderDatalist"
        :orderId="orderId"
        :row-active="rowActive"
        :formType="1"
    ></details-from>
    <!-- 会员详情-->
    <user-details ref="userDetails" fromType="order"></user-details>
    <!-- 备注 -->
    <order-remark
        ref="remarks"
        :orderId="orderId"
        @submitFail="submitFail"
    ></order-remark>
    <!-- 记录 -->
    <order-record ref="record"></order-record>
    <!-- 发送货 -->
    <order-send
        ref="send"
        :orderId="orderId"
        @submitFail="submitFail"
    >
    </order-send>
    <Modal
        v-model="manualModal"
        title="手动批量发货"
        @on-ok="manualModalOk"
        @on-cancel="manualModalCancel"
        class-name="vertical-center-modal"
    >
      <Row type="flex">
        <Col span="4">
          <div style="line-height: 32px; text-align: right">文件：</div>
        </Col>
        <Col span="20">
          <Upload
              ref="upload"
              :action="uploadAction"
              :headers="uploadHeaders"
              accept=".xlsx,.xls"
              :format="['xlsx', 'xls']"
              :disabled="!!fileList.length"
              :on-success="uploadSuccess"
              :on-remove="removeFile"
          >
            <Button icon="ios-cloud-upload-outline">上传文件</Button>
          </Upload>
        </Col>
      </Row>
    </Modal>
    <!--订单核销模态框-->
    <Modal
        v-model="modals2"
        title="订单核销"
        class="paymentFooter"
        scrollable
        width="400"
        class-name="vertical-center-modal"
    >
      <Form
          ref="writeOffFrom"
          :model="writeOffFrom"
          :rules="writeOffRules"
          :label-position="labelPosition"
          class="tabform"
          @submit.native.prevent
      >
        <FormItem prop="code" label-for="code">
          <Input
              search
              enter-button="验证"
              style="width: 100%"
              type="text"
              placeholder="请输入12位核销码"
              @on-search="search('writeOffFrom')"
              v-model.number="writeOffFrom.code"
              number
          />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="primary" @click="ok">立即核销</Button>
        <Button @click="del('writeOffFrom')">取消</Button>
      </div>
    </Modal>
    <auto-send
        ref="sends"
        :selectArr="checkUidList"
        :isAll="isAll"
    ></auto-send>
    <queue-list ref="queue"></queue-list>
  </div>
</template>

<script>
import { mapState } from "vuex";
import editFrom from "@/components/from/from";
import orderSend from "./components/orderSend";
import detailsFrom from "./components/orderDetails";
import orderRecord from "./components/orderRecord";
import orderRemark from "./components/orderRemark";
import userDetails from "@/components/userDetails/userDetails";
import autoSend from "./components/autoSend";
import queueList from "./components/queueList";
import timeOptions from "@/utils/timeOptions";
import util from "@/libs/util";
import Setting from "@/setting";
import exportExcel from "@/utils/newToExcel.js";
import expandRow from "./components/tableExpand.vue";
import {
  orderList,
  getOrdeDatas,
  getDataInfo,
  getRefundFrom,
  refundIntegral,
  getnoRefund,
  getDistribution,
  writeUpdate,
  storeOrderApi,
  handBatchDelivery,
  putWrite,
  exportExpressList
} from "@/api/order";
export default {
  name: "index",
  components: {
    editFrom,
    detailsFrom,
    orderRecord,
    orderRemark,
    orderSend,
    userDetails,
    autoSend,
    queueList
  },
  data() {
    const codeNum = (rule, value, callback) => {
      if (!value) {
        return callback(new Error("请填写核销码"));
      }
      // 模拟异步验证效果
      if (!Number.isInteger(value)) {
        callback(new Error("请填写12位数字"));
      } else {
        const reg = /\b\d{12}\b/;
        if (!reg.test(value)) {
          callback(new Error("请填写12位数字"));
        } else {
          callback();
        }
      }
    };
    return {
      isAll: 0,
      isCheckBox: false,
      checkUidList:[],
      manualModal:false,
      timeVal: [],
      options: timeOptions,
      payList: [
        { label: "全部", val: "" },
        { label: "微信支付", val: "1" },
        { label: "支付宝支付", val: "4" },
        { label: "余额支付", val: "2" },
        { label: "线下支付", val: "3" },
      ],
      // 订单列表
      orderData: {
        page: 1,
        limit: 10,
        type: "",
        status: "",
        data: "",
        real_name: "",
        pay_type: "",
        field_key: "all"
      },
      orderList: [],
      total: 0,
      loading: false,
      orderConNum:0,
      orderConId:0,
      orderId: 0,
      delfromData: {},
      rowActive: {},
      orderDatalist: null,
      FromData: null,
      file: "",
      uploadAction: `${Setting.apiBaseURL}/file/upload/1`,
      uploadHeaders: {},
      fileList: [],
      modals2: false,
      writeOffRules: {
        code: [{ validator: codeNum, trigger: "blur", required: true }],
      },
      writeOffFrom: {
        code: "",
        confirm: 0,
      },
      exportListOn: 0,
      exportList: [
        {
          name: "1",
          label: "导出发货单",
        },
        {
          name: "0",
          label: "导出订单",
        },
      ],
    };
  },
  watch: {
    $route() {
      if (this.$route.fullPath === `${Setting.roterPre}/order/list?type=7&status=1`) {
        this.getPath();
      }
    }
  },
  computed: {
    ...mapState("store/layout", ["isMobile"]),
    labelWidth() {
      return this.isMobile ? undefined : 80;
    },
    labelPosition() {
      return this.isMobile ? "top" : "right";
    },
  },
  created() {
    this.getToken();
    if (this.$route.fullPath === `${Setting.roterPre}/order/list?type=7&status=1`) {
      this.getPath();
    } else {
      this.getList();
    }
  },
  mounted() {

  },
  methods: {
    //跳转刷新
    getPath() {
      this.orderData.page = 1;
      this.orderData.status = this.$route.query.status;
      this.getList();
    },
    allReset(){
      this.isAll = 0;
      this.isCheckBox = false;
      this.$refs.xTable.setAllCheckboxRow(false);
      this.checkUidList = [];
    },
    allPages(e){
      this.isAll = e;
      if(e==0){
        this.$refs.xTable.toggleAllCheckboxRow();
        // this.checkboxAll();
      }else{
        if(!this.isCheckBox){
          this.$refs.xTable.setAllCheckboxRow(true);
          this.isCheckBox = true;
          this.isAll = 1;
        }else{
          this.$refs.xTable.setAllCheckboxRow(false);
          this.isCheckBox = false;
          this.isAll = 0;
        }
        this.checkUidList = []
      }
    },
    checkboxItem(e){
      let id = parseInt(e.rowid);
      let index = this.checkUidList.indexOf(id);
      if(index !== -1){
        this.checkUidList = this.checkUidList.filter((item)=> item !== id);
      }else{
        this.checkUidList.push(id);
      }
    },
    checkboxAll(){
      // 获取选中当前值
      let obj2 = this.$refs.xTable.getCheckboxRecords(true);
      // 获取之前选中值
      let obj = this.$refs.xTable.getCheckboxReserveRecords(true);
      if(this.isAll == 0 && this.checkUidList.length <= obj.length && !this.isCheckBox){
        obj = [];
      }
      obj = obj.concat(obj2);
      let ids = [];
      obj.forEach((item)=>{
        ids.push(parseInt(item.id))
      })
      this.checkUidList = ids;
      if(!obj2.length){
        this.isCheckBox = false;
      }
    },
    printOreder(){
      if (this.checkUidList.length > 10 || (this.isAll==1 && this.total>10)) {
        return this.$Message.error('最多批量打印10个订单')
      }
      let ids = []
      if (this.isAll==1 && this.total<=10){
        this.orderList.forEach(item=>{
          ids.push(parseInt(item.id))
        })
      }
      let pathInfo = this.$router.resolve({
        path:`/supplier/order/distribution`,
        query:{
          id: this.isAll==1?ids.join(','):this.checkUidList.join(','),
        }
      });
      window.open(pathInfo.href, '_blank');
    },
    reset(){
      this.timeVal = [];
      this.orderData = {
        page: 1,
        limit: 10,
        type: "",
        status: "",
        data: "",
        real_name: "",
        pay_type: ""
      };
      this.getList();
    },
    queuemModal() {
      this.$refs.queue.modal = true;
    },
    delAll(){
      if (this.checkUidList.length === 0 && this.isAll==0) {
        return this.$Message.error('请先选择删除的订单！')
      }
      let idss = {
        all: this.isAll,
        ids: this.checkUidList,
      }
      let delfromData = {
        title: "删除订单",
        url: `/order/dels`,
        method: "post",
        ids: idss,
      };
      this.$modalSure(delfromData).then((res) => {
        this.$Message.success(res.msg);
        this.allReset();
        this.getList();
      }).catch((res) => {
        this.$Message.error(res.msg);
      });
    },
    onAuto(){
      this.$refs.sends.modals = true;
      this.$refs.sends.getList();
      this.$refs.sends.getDeliveryList();
    },
    // 订单导出
    async exports(value) {
      this.exportListOn = this.exportList.findIndex(
          (item) => item.name === value
      );
      let [th, filekey, data, fileName] = [[], [], [], ""];
      let excelData = JSON.parse(JSON.stringify(this.orderData));
      excelData.page = 1;
      excelData.type = value;
      excelData.ids = this.checkUidList.join();
      for (let i = 0; i < excelData.page + 1; i++) {
        let lebData = await this.downOrderData(excelData);
        if (!fileName) fileName = lebData.filename;
        if (!filekey.length) {
          filekey = lebData.filekey;
        }
        if (!th.length) th = lebData.header;
        if (lebData.export.length) {
          data = data.concat(lebData.export);
          excelData.page++;
        } else {
          exportExcel(th, filekey, fileName, data);
          return;
        }
      }
    },
    downOrderData(excelData) {
      return new Promise((resolve, reject) => {
        storeOrderApi(excelData).then((res) => {
          return resolve(res.data);
        });
      });
    },
    // 订单核销
    writeOff() {
      this.modals2 = true;
    },
    // 验证
    search(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          this.writeOffFrom.confirm = 0;
          putWrite(this.writeOffFrom)
              .then(async (res) => {
                if (res.status === 200) {
                  this.$Message.success(res.msg);
                } else {
                  this.$Message.error(res.msg);
                }
              })
              .catch((res) => {
                this.$Message.error(res.msg);
              });
        } else {
          this.$Message.error("请填写正确的核销码");
        }
      });
    },
    // 订单核销
    ok() {
      if (!this.writeOffFrom.code) {
        this.$Message.warning("请先验证订单！");
      } else {
        this.writeOffFrom.confirm = 1;
        putWrite(this.writeOffFrom)
            .then(async (res) => {
              if (res.status === 200) {
                this.$Message.success(res.msg);
                this.modals2 = false;
                this.$refs[name].resetFields();
                this.getList()
              } else {
                this.$Message.error(res.msg);
              }
            })
            .catch((res) => {
              this.$Message.error(res.msg);
            });
      }
    },
    del(name) {
      this.modals2 = false;
      this.writeOffFrom.confirm = 0;
      this.$refs[name].resetFields();
    },
    // 上传头部token
    getToken() {
      this.uploadHeaders["Authori-zation"] = "Bearer " + util.cookies.get("token");
    },
    // 上传成功
    uploadSuccess(res, file, fileList) {
      if (res.status === 200) {
        this.$Message.success(res.msg);
        this.file = res.data.src;
        this.fileList = fileList;
      } else {
        this.$Message.error(res.msg);
      }
    },
    //移除文件
    removeFile(file, fileList) {
      this.file = "";
      this.fileList = fileList;
    },
    // 下载物流公司对照表
    async getExpressList() {
      let [th, filekey, data, fileName] = [[], [], [], ""];
      let lebData = await this.getExcelData();
      if (!fileName) fileName = lebData.filename;
      if (!filekey.length) {
        filekey = lebData.filekey;
      }
      if (!th.length) th = lebData.header;
      data = lebData.export;
      exportExcel(th, filekey, fileName, data);
    },
    getExcelData() {
      return new Promise((resolve, reject) => {
        exportExpressList().then((res) => {
          return resolve(res.data);
        });
      });
    },
    // 手动批量发货-确定
    manualModalOk() {
      this.$refs.upload.clearFiles();
      handBatchDelivery({ file: this.file,}).then((res) => {
        this.$Message.success(res.msg);
        this.fileList = [];
      }).catch((err) => {
        this.$Message.error(err.msg);
        this.fileList = [];
      });
    },
    // 手动批量发货-取消
    manualModalCancel() {
      this.fileList = [];
      this.$refs.upload.clearFiles();
    },
    // 核销订单
    bindWrite(row) {
      let self = this;
      this.$Modal.confirm({
        title: "提示",
        content: "确定要核销该订单吗？",
        cancelText: "取消",
        closable: true,
        maskClosable: true,
        onOk: function () {
          writeUpdate(row.order_id).then((res) => {
            self.$Message.success(res.msg);
            self.getList();
          });
        },
        onCancel: () => {},
      });
    },
    // 配送信息表单数据
    delivery(row,num) {
      getDistribution(row.id).then(async (res) => {
        this.orderConNum = num;
        this.orderConId = row.pid;
        this.FromData = res.data;
        this.$refs.edits.modals = true;
        if(num !=1){
          this.getData(this.orderId,1);
        }
      }).catch((res) => {
        this.$Message.error(res.msg);
      });
    },
    // 编辑
    edit(row) {
      this.getOrderData(row.id);
    },
    // 获取编辑表单数据
    getOrderData(id) {
      getOrdeDatas(id).then(async (res) => {
        if (res.data.status === false) {
          return this.$authLapse(res.data);
        }
        this.$authLapse(res.data);
        this.FromData = res.data;
        this.$refs.edits.modals = true;
      }).catch((res) => {
        this.$Message.error(res.msg);
      });
    },
    // 发送货
    sendOrder(row,num) {
      this.orderConId = row.pid;
      this.orderConNum = num;
      this.$store.commit("store/order/setSplitOrder", row.total_num);
      this.$refs.send.modals = true;
      this.orderId = row.id;
      this.$refs.send.getList();
      this.$refs.send.getDeliveryList();
      this.$nextTick((e) => {
        this.$refs.send.getCartInfo(row._status, row.id);
      });
    },
    // 修改成功
    submitFail() {
      this.getList();
      if(this.orderConNum !=1){
        this.getData(this.orderId,1);
      }else{
        this.$refs.detailss.getSplitOrder(this.orderConId);
      }
    },
    // 操作
    changeMenu(row, name, num) {
      this.orderId = row.id;
      this.orderConId = row.pid>0?row.pid:row.id;
      this.orderConNum = num;
      switch (name) {
        case "1":
          this.delfromData = {
            title: "修改立即支付",
            url: `/order/pay_offline/${row.id}`,
            method: "post",
            ids: "",
          };
          this.$modalSure(this.delfromData)
              .then((res) => {
                this.$Message.success(res.msg);
                this.getData(row.id,1);
                this.getList();
              })
              .catch((res) => {
                this.$Message.error(res.msg);
              });
          break;
        case "2":
          this.rowActive = row;
          this.getData(row.id);
          break;
        case "3":
          this.$refs.record.modals = true;
          this.$refs.record.getList(row.id);
          break;
        case "4":
          this.$refs.remarks.formValidate.remark = row.remark;
          this.$refs.remarks.modals = true;
          break;
        case "5":
          this.getOnlyRefundData(row.id, row.refund_type);
          break;
        case "55":
          this.getRefundData(row.id, row.refund_type);
          break;
        case "6":
          this.getRefundIntegral(row.id);
          break;
        case "7":
          this.getNoRefundData(row.id);
          break;
        case "8":
          this.delfromData = {
            title: "修改确认收货",
            url: `/order/take/${row.id}`,
            method: "put",
            ids: "",
          };
          this.$modalSure(this.delfromData)
              .then((res) => {
                this.$Message.success(res.msg);
                this.getList();
                if(num){
                  this.$refs.detailss.getSplitOrder(row.pid)
                }else{
                  this.getData(row.id,1);
                }
              })
              .catch((res) => {
                this.$Message.error(res.msg);
              });
          break;
        case "10":
          this.delfromData = {
            title: "立即打印订单",
            info: "您确认打印此订单吗?",
            url: `/order/print/${row.id}`,
            method: "get",
            ids: "",
          };
          this.$modalSure(this.delfromData)
              .then((res) => {
                this.$Message.success(res.msg);
                this.getList();
              })
              .catch((res) => {
                this.$Message.error(res.msg);
              });
          break;
        case "11":
          this.delfromData = {
            title: "立即打印电子面单",
            info: "您确认打印此电子面单吗?",
            url: `/order/order_dump/${row.id}`,
            method: "get",
            ids: "",
          };
          this.$modalSure(this.delfromData)
              .then((res) => {
                this.$Message.success(res.msg);
                this.getList();
              })
              .catch((res) => {
                this.$Message.error(res.msg);
              });
          break;
        case "12":
          let pathInfo = this.$router.resolve({
            path:`/supplier/order/distribution`,
            query:{
              id:row.id
            }
          });
          window.open(pathInfo.href, '_blank');
          break;
        default:
          this.delfromData = {
            title: "删除订单",
            url: `/order/del/${row.id}`,
            method: "DELETE",
            ids: "",
          };
          this.delOrder(row, this.delfromData);
      }
    },
    // 获取详情表单数据
    getData(id,type) {
      getDataInfo(id)
          .then(async (res) => {
            if(!type){
              this.$refs.detailss.modals = true;
            }
            this.$refs.detailss.activeName = 'detail';
            this.orderDatalist = res.data;
            if (this.orderDatalist.orderInfo.refund_reason_wap_img) {
              try {
                this.orderDatalist.orderInfo.refund_reason_wap_img = JSON.parse(
                    this.orderDatalist.orderInfo.refund_reason_wap_img
                );
              } catch (e) {
                this.orderDatalist.orderInfo.refund_reason_wap_img = [];
              }
            }
          })
          .catch((res) => {
            this.$Message.error(res.msg);
          });
    },
    // 仅退款
    getOnlyRefundData(id, refund_type) {
      this.$modalForm(getRefundFrom(id)).then(() => {
        this.getList();
        this.$refs.detailss.modals = false;
      });
    },
    // 退货退款
    getRefundData(id, refund_type) {
      this.delfromData = {
        title: "是否立即退货退款",
        url: `/refund/agree/${id}`,
        method: "get",
      };
      this.$modalSure(this.delfromData)
          .then((res) => {
            this.$Message.success(res.msg);
            this.getList();
          })
          .catch((res) => {
            this.$Message.error(res.msg);
          });
    },
    // 获取退积分表单数据
    getRefundIntegral(id) {
      refundIntegral(id)
          .then(async (res) => {
            this.FromData = res.data;
            this.$refs.edits.modals = true;
          })
          .catch((res) => {
            this.$Message.error(res.msg);
          });
    },
    // 不退款表单数据
    getNoRefundData(id) {
      this.$modalForm(getnoRefund(id)).then(() => {
        this.getList();
      });
    },
    // 删除单条订单
    delOrder(row, data) {
      if (row.is_del === 1) {
        this.$modalSure(data)
            .then((res) => {
              this.$Message.success(res.msg);
              this.getList();
              this.$refs.detailss.modals = false;
            })
            .catch((res) => {
              this.$Message.error(res.msg);
            });
      } else {
        const title = "错误！";
        const content =
            "<p>您选择的的订单存在用户未删除的订单，无法删除用户未删除的订单！</p>";
        this.$Modal.error({
          title: title,
          content: content,
        });
      }
    },
    getList(){
      this.loading = true;
      orderList(this.orderData).then(res=>{
        let data = res.data;
        data.data.forEach((item)=>{
          if(item.id == this.orderId){
            this.rowActive = item;
          }
        });
        this.$set(this,'orderList',data.data);
        this.total = res.data.count;
        this.loading = false;
        this.$nextTick(function(){
          if (this.isAll == 1) {
            if(this.isCheckBox){
              this.$refs.xTable.setAllCheckboxRow(true);
            }else{
              this.$refs.xTable.setAllCheckboxRow(false);
            }
          }else{
            let obj = this.$refs.xTable.getCheckboxReserveRecords(true);
            if(!this.checkUidList.length || this.checkUidList.length <= obj.length){
              this.$refs.xTable.setAllCheckboxRow(false);
            }
          }
        })
      }).catch(err=>{
        this.loading = false;
        this.$Message.error(err.msg)
      })
    },
    // 具体日期
    onchangeTime(e) {
      if (e[1].slice(-8) === "00:00:00") {
        e[1] = e[1].slice(0, -8) + "23:59:59";
        this.timeVal = e;
      } else {
        this.timeVal = e;
      }
      this.orderData.data = this.timeVal[0] ? this.timeVal.join("-") : "";
      this.orderData.page = 1;
      this.allReset();
      this.getList();
    },
    showUserInfo(row) {
      this.$refs.userDetails.modals = true;
      this.$refs.userDetails.activeName = 'info';
      this.$refs.userDetails.getDetails(row.uid);
    },
    pageChange(index) {
      this.orderData.page = index;
      this.getList();
    },
    userSearchs(){
      this.allReset();
      this.orderData.page = 1;
      this.getList();
    }
  },
};
</script>

<style lang="stylus" scoped>
.tdinfo{
  margin-left: 75px;
  margin-top: 16px;
}
.expand-row{
  margin-bottom: 16px;
  font-size: 12px;
}
.tabBox {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;

  .tabBox_img {
    width: 30px;
    height: 30px;

    img {
      width: 100%;
      height: 100%;
    }
  }

  .tabBox_tit {
    width:267px;
    height:30px;
    line-height:30px;
    font-size: 12px !important;
    margin: 0 2px 0 10px;
    letter-spacing: 1px;
    box-sizing: border-box;
  }
}
.tabBox +.tabBox{
  margin-top:5px;
}
</style>
