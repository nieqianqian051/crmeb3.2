<template>
  <!-- 营销-抽奖配置 -->
  <div class="form-submit">
    <Card :bordered="false" dis-hover class="ivu-mt mb79">
      <div class="new_tab">
        <!-- 顶部Tab栏切换 -->
        <Tabs v-model="formValidate.factor" @on-click="lotteryType">
          <TabPane
            v-for="(item, index) in headTab"
            :key="index"
            :label="item.name"
            :name="item.type"
          ></TabPane>
        </Tabs>
      </div>
      <Row type="flex">
        <Col span="23">
          <Form
            class="form"
            ref="formValidate"
            :rules="ruleValidate"
            :model="formValidate"
            :label-width="labelWidth"
            :label-position="labelPosition"
            @submit.native.prevent
          >
            <Row type="flex">
              <Col span="24">
                <FormItem label="活动名称：" prop="name" label-for="name">
                  <Input
                    class="perW30"
                    placeholder="请输入活动名称"
                    element-id="name"
                    v-model="formValidate.name"
                  />
                </FormItem>
              </Col>
              <Col span="24">
                <FormItem label="活动时间：" prop="period">
                  <div class="acea-row row-middle">
                    <DatePicker
                      :editable="false"
                      type="datetimerange"
                      format="yyyy-MM-dd HH:mm"
                      placeholder="请选择活动时间"
                      @on-change="onchangeTime"
                      class="perW30"
                      v-model="formValidate.period"
                    ></DatePicker>
                  </div>
                </FormItem>
              </Col>
              <Col v-if="formValidate.factor == 1" span="24">
                <FormItem label="抽奖类型：" prop="type" label-for="type">
                  <RadioGroup element-id="type" v-model="formValidate.type">
                    <Radio :label="1" class="radio">九宫格</Radio>
                    <Radio :label="2">大转盘</Radio>
                  </RadioGroup>
                </FormItem>
              </Col>
              <Col span="24">
                <FormItem
                  label="参与用户："
                  prop="attends_user"
                  label-for="attends_user"
                >
                  <RadioGroup
                    element-id="attends_user"
                    v-model="formValidate.attends_user"
                  >
                    <Radio :label="1" class="radio">全部用户</Radio>
                    <Radio :label="2">部分用户</Radio>
                  </RadioGroup>
                </FormItem>
              </Col>
              <Col span="24" v-if="formValidate.attends_user == 2">
                <FormItem
                  label=""
                  :prop="formValidate.attends_user == 2 ? 'user_level' : ''"
                >
                  <div class="acea-row row-middle">
                    <Select
                      multiple
                      v-model="formValidate.user_level"
                      class="perW30"
                      placeholder="请选择用户等级"
                    >
                      <Option
                        v-for="item in userLevelListApi"
                        :value="String(item.id)"
                        :key="item.id"
                        >{{ item.name }}</Option
                      >
                    </Select>
                  </div>
                </FormItem>
              </Col>
              <Col span="24" v-if="formValidate.attends_user == 2">
                <FormItem
                  label=""
                  :prop="formValidate.attends_user == 2 ? 'is_svip' : ''"
                >
                  <div class="acea-row row-middle">
                    <Select
                      v-model="formValidate.is_svip"
                      class="perW30"
                      clearable
                      placeholder="请选择是否是付费会员"
                    >
                      <Option
                        v-for="item in templateList"
                        :value="item.id"
                        :key="item.id"
                        >{{ item.name }}</Option
                      >
                    </Select>
                  </div>
                </FormItem>
              </Col>
              <Col span="24" v-if="formValidate.attends_user == 2">
                <FormItem
                  label=""
                  :prop="formValidate.attends_user == 2 ? 'user_label' : ''"
                >
                  <div
                    class="labelInput acea-row row-between-wrapper"
                    @click="openLabel"
                  >
                    <div style="width: 90%;">
                      <div v-if="dataLabel.length">
                        <Tag
                          closable
                          v-for="(item, index) in dataLabel"
                          :key="index"
                          @on-close="closeLabel(item)"
                          >{{ item.label_name }}</Tag
                        >
                      </div>
                      <span class="span" v-else>选择用户关联标签</span>
                    </div>
                    <div class="iconfont iconxiayi"></div>
                  </div>
                  <div class="ml100 grey">
                    三个条件都设置后,必须这些条件都满足的用户才能参加抽奖
                  </div>
                </FormItem>
              </Col>
              <Col
                span="24"
                v-if="formValidate.factor == 1"
                key="total_lottery_num"
              >
                <FormItem
                  label="每人抽奖总次数："
                  :prop="formValidate.factor == 1 ? 'total_lottery_num' : ''"
                  label-for="total_lottery_num"
                >
                  <div class="input-number-wrapper perW20">
                    <InputNumber
                      v-model="formValidate.total_lottery_num"
                      :min="1"
                      element-id="total_lottery_num"
                    ></InputNumber>
                    <div class="suffix">次</div>
                  </div>
                </FormItem>
              </Col>
              <Col span="24" v-if="formValidate.factor == 1" key="lottery_num2">
                <FormItem
                  label="每人每天抽奖次数："
                  :prop="formValidate.factor == 1 ? 'lottery_num' : ''"
                  label-for="lottery_num2"
                >
                  <div class="input-number-wrapper perW20">
                    <InputNumber
                      v-model="formValidate.lottery_num"
                      :min="1"
                      element-id="lottery_num2"
                    ></InputNumber>
                    <div class="suffix">次</div>
                  </div>
                </FormItem>
              </Col>
              <Col span="24" v-if="formValidate.factor == 5">
                <FormItem
                  label="抽奖次数："
                  :prop="formValidate.factor == 5 ? 'lottery_num_term' : ''"
                  label-for="status"
                >
                  <RadioGroup
                    element-id="lottery_num_term"
                    v-model="formValidate.lottery_num_term"
                  >
                    <Radio :label="1" class="radio">每天N次</Radio>
                    <Radio :label="2">每人N次</Radio>
                  </RadioGroup>
                </FormItem>
              </Col>
              <Col span="24" v-if="formValidate.factor == 5">
                <FormItem
                  label="邀请新用户最多可获得抽奖"
                  :prop="formValidate.factor == 5 ? 'lottery_num' : ''"
                  label-for="lottery_num"
                >
                  <div class="acea-row row-middle">
                    <div class="mr10 grey"></div>
                    <InputNumber
                      placeholder=""
                      element-id="lottery_num"
                      :min="1"
                      :precision="0"
                      v-model="formValidate.lottery_num"
                      class="perW20"
                    />
                    <div class="ml10 grey">次</div>
                  </div>
                </FormItem>
              </Col>
              <Col span="24" v-if="formValidate.factor == 5">
                <FormItem
                  label="邀请一位新用户关注公众号可获得抽奖"
                  :prop="formValidate.factor == 5 ? 'spread_num' : ''"
                  label-for="spread_num"
                >
                  <div class="acea-row row-middle">
                    <div class="mr10 grey"></div>
                    <InputNumber
                      placeholder=""
                      element-id="spread_num"
                      :min="1"
                      :precision="0"
                      v-model="formValidate.spread_num"
                      class="perW20"
                    />
                    <div class="ml10 grey">次</div>
                  </div>
                </FormItem>
              </Col>
              <Col
                span="24"
                v-if="
                  formValidate.factor == 1 ||
                    formValidate.factor == 3 ||
                    formValidate.factor == 4
                "
                key="factor_num"
              >
                <FormItem
                  :label="
                    formValidate.factor == 1
                      ? '单次抽奖消耗积分：'
                      : '抽奖次数：'
                  "
                  :prop="
                    formValidate.factor == 1 ||
                    formValidate.factor == 3 ||
                    formValidate.factor == 4
                      ? 'factor_num'
                      : ''
                  "
                  label-for="factor_num"
                >
                  <div class="acea-row row-middle">
                    <div class="mr10 grey"></div>
                    <InputNumber
                      placeholder=""
                      element-id="factor_num"
                      :min="1"
                      :precision="0"
                      v-model="formValidate.factor_num"
                      class="perW20"
                    />
                    <div class="ml10 grey" v-if="formValidate.factor != 1">
                      次
                    </div>
                  </div>
                </FormItem>
              </Col>
            </Row>
            <Row>
              <Col span="24">
                <FormItem label="规则选择：" prop="prize">
                  <Table
                    :data="specsData"
                    :columns="columns"
                    border
                    :draggable="true"
                    @on-drag-drop="onDragDrop"
                  >
                    <template slot-scope="{ row, index }" slot="image">
                      <div
                        class="acea-row row-middle row-center-wrapper"
                        @click="modalPicTap('dan', 'goods', index)"
                      >
                        <div class="pictrue pictrueTab" v-if="row.image">
                          <img v-lazy="row.image" />
                        </div>
                        <div
                          class="upLoad pictrueTab acea-row row-center-wrapper"
                          v-else
                        >
                          <Icon
                            type="ios-camera-outline"
                            size="21"
                            class="iconfonts"
                          />
                        </div>
                      </div>
                    </template>
                    <template slot-scope="{ row, index }" slot="total">
                      <InputNumber
                        v-model="row.total"
                        :max="99999"
                        :min="0"
                        :precision="0"
                        class="priceBox"
                        @on-change="
                          (data) => {
                            changeTotal(data, index);
                          }
                        "
                      ></InputNumber>
                    </template>
                    <template slot-scope="{ row, index }" slot="chance">
                      <InputNumber
                        v-model="row.chance"
                        :max="1000"
                        :min="0"
                        :precision="0"
                        class="priceBox"
                        @on-change="
                          (data) => {
                            changeChance(data, index);
                          }
                        "
                      ></InputNumber>
                    </template>
                    <template slot-scope="{ row, index }" slot="type">
                      <div>{{ row.type | typeName }}</div>
                    </template>
                    <template slot-scope="{ row, index }" slot="setting">
                      <Button
                        class="submission"
                        type="text"
                        style="
                          background-color: transparent;
                          box-shadow: none;
                          color: #1890ff;
                        "
                        @click="editGoods(index)"
                        >编辑</Button
                      >
                    </template>
                  </Table>
                  <Button
                    v-if="specsData.length < 8"
                    type="primary"
                    class="submission mr15 mt20"
                    @click="addGoods"
                    >添加商品</Button
                  >
                </FormItem>
                <FormItem>
                  <div class="pl60 grey">
                    奖品必须设置为8个，列表中拖拽可调整奖品在九宫中的位置
                    <Poptip
                      placement="bottom"
                      trigger="hover"
                      width="380"
                      transfer
                    >
                      <a>查看位置示例图</a>
                      <div class="api" slot="content">
                        <img
                          src="../../../assets/images/lotteryTest.png"
                          alt=""
                        />
                      </div>
                    </Poptip>
                  </div>
                </FormItem>
              </Col>
            </Row>
            <div>
              <FormItem
                v-if="formValidate.factor != 3 && formValidate.factor != 4"
                :prop="
                  formValidate.factor != 3 && formValidate.factor != 4
                    ? 'image'
                    : ''
                "
              >
                <div class="custom-label" slot="label">
                  <div>
                    <div>活动背景图</div>
                  </div>
                  <div>：</div>
                </div>
                <div class="acea-row">
                  <div class="pictrue" v-if="formValidate.image">
                    <img v-lazy="formValidate.image" />
                    <Button
                      shape="circle"
                      icon="md-close"
                      @click.native="handleRemove()"
                      class="btndel"
                    ></Button>
                  </div>
                  <div
                    v-else
                    class="upLoad acea-row row-center-wrapper"
                    @click="modalPicTap('dan', 'danFrom')"
                  >
                    <Icon
                      type="ios-camera-outline"
                      size="26"
                      class="iconfonts"
                    />
                  </div>
                </div>
                <div class="fs-12 text--w111-999">
                  建议尺寸：750*1334
                  <Poptip
                    placement="bottom"
                    trigger="hover"
                    width="256"
                    transfer
                    padding="8px"
                  >
                    <a>查看示例</a>
                    <div class="exampleImg" slot="content">
                      <img :src="`${baseURL}/statics/system/luck.png`" alt="" />
                    </div>
                  </Poptip>
                </div>
              </FormItem>
              <FormItem
                v-if="formValidate.factor != 3 && formValidate.factor != 4"
                label="中奖名单："
                :prop="
                  formValidate.factor != 3 && formValidate.factor != 4
                    ? 'is_all_record'
                    : ''
                "
                label-for="is_all_record"
              >
                <Switch
                  v-model="formValidate.is_all_record"
                  :true-value="1"
                  :false-value="0"
                  element-id="is_all_record"
                  size="large"
                >
                  <span slot="open">开启</span>
                  <span slot="close">关闭</span>
                </Switch>
              </FormItem>
              <FormItem
                v-if="formValidate.factor != 3 && formValidate.factor != 4"
                label="个人中奖记录："
                :prop="
                  formValidate.factor != 3 && formValidate.factor != 4
                    ? 'is_personal_record'
                    : ''
                "
                label-for="is_personal_record"
              >
                <Switch
                  v-model="formValidate.is_personal_record"
                  :true-value="1"
                  :false-value="0"
                  element-id="is_personal_record"
                  size="large"
                >
                  <span slot="open">开启</span>
                  <span slot="close">关闭</span>
                </Switch>
              </FormItem>
              <FormItem
                v-if="formValidate.factor != 3 && formValidate.factor != 4"
                label="活动规则："
                prop="is_content"
                label-for="is_content"
              >
                <Switch
                  v-model="formValidate.is_content"
                  :true-value="1"
                  :false-value="0"
                  element-id="is_content"
                  size="large"
                >
                  <span slot="open">开启</span>
                  <span slot="close">关闭</span>
                </Switch>
              </FormItem>
              <FormItem
                label=""
                :prop="
                  formValidate.factor != 3 &&
                  formValidate.factor != 4 &&
                  formValidate.is_content == 1
                    ? 'content'
                    : ''
                "
                v-if="
                  formValidate.factor != 3 &&
                    formValidate.factor != 4 &&
                    formValidate.is_content == 1
                "
              >
                <WangEditor
                  style="width: 90%"
                  :content="formValidate.content"
                  @editorContent="getEditorContent"
                ></WangEditor>
              </FormItem>
              <FormItem label="活动状态：" prop="status" label-for="status">
                <Switch
                  v-model="formValidate.status"
                  :true-value="1"
                  :false-value="0"
                  element-id="status"
                  size="large"
                >
                  <span slot="open">开启</span>
                  <span slot="close">关闭</span>
                </Switch>
              </FormItem>
            </div>
            <Spin size="large" fix v-if="spinShow"></Spin>
          </Form>
        </Col>
      </Row>
    </Card>
    <Card
      :bordered="false"
      dis-hover
      class="fixed-card"
      :style="{ left: `${!menuCollapse ? '200px' : isMobile ? '0' : '80px'}` }"
    >
      <Form>
        <FormItem class="submit-form-item">
          <Button
            type="primary"
            class="submission"
            :loading="submitOpen"
            @click="next('formValidate')"
          >
            <div v-if="!submitOpen">提交</div>
            <div v-else>提交中</div>
          </Button>
        </FormItem>
      </Form>
    </Card>
    <!-- 用户标签 -->
    <Modal
      v-model="labelShow"
      scrollable
      title="选择用户标签"
      :closable="true"
      width="540"
      :footer-hide="true"
      :mask-closable="false"
    >
      <userLabel
        ref="userLabel"
        @activeData="activeData"
        @close="labelClose"
      ></userLabel>
    </Modal>
    <!-- 上传图片-->
    <Modal
      v-model="modalPic"
      width="960px"
      scrollable
      footer-hide
      closable
      title="上传商品图"
      :mask-closable="false"
      :z-index="1"
    >
      <uploadPictures
        :isChoice="isChoice"
        @getPic="getPic"
        v-if="modalPic"
      ></uploadPictures>
    </Modal>
    <!-- 上传图片-->
    <Modal
      v-model="addGoodsModel"
      width="60%"
      scrollable
      footer-hide
      closable
      :title="title"
      :mask-closable="false"
      :z-index="1"
    >
      <addGoods
        v-if="addGoodsModel"
        @addGoodsData="addGoodsData"
        :editData="editData"
      ></addGoods>
    </Modal>
  </div>
</template>

<script>
import Setting from "@/setting";
import { mapMutations, mapState } from "vuex";
import goodsList from "@/components/goodsList/index";
import uploadPictures from "@/components/uploadPictures";
import userLabel from "@/components/labelList";
import addGoods from "./addGoods";
import {
  lotteryDetailApi,
  lotteryCreateApi,
  lotteryEditApi,
  lotteryFactorInfo,
} from "@/api/lottery"; //详情 创建 编辑
import { lotteryFrom } from "./formRule/lotteryFrom";
import { labelListApi } from "@/api/product";
import { levelListApi } from "@/api/user";
import WangEditor from "@/components/wangEditor/index.vue";
import { formatDate } from "@/utils/validate";
import { formatRichText } from "@/utils/editorImg";

export default {
  name: "lotteryCreate",
  components: { goodsList, uploadPictures, WangEditor, addGoods, userLabel },
  data() {
    Object.keys(lotteryFrom).forEach((key) => {
      if (key === "total_lottery_num") {
        lotteryFrom[key][0]["validator"] = (rule, value, callback) => {
          if (value < this.formValidate.lottery_num) {
            return callback(new Error("总次数不能小于每天抽奖次数"));
          }
          callback();
        };
      }
    });
    return {
      baseURL: Setting.apiBaseURL.replace(/adminapi/, ""),
      dataLabel: [],
      labelShow: false,
      headTab: [
        {
          name: "积分抽奖",
          type: "1",
        },
        {
          name: "订单支付",
          type: "3",
        },
        {
          name: "订单评价",
          type: "4",
        },
        {
          name: "关注公众号",
          type: "5",
        },
      ],
      title: "添加商品",
      loading: false,
      userLabelList: [], //用户标签列表
      userLevelListApi: [], //用户等级列表
      submitOpen: false,
      spinShow: false,
      addGoodsModel: false,
      editData: {},
      myConfig: {
        autoHeightEnabled: false, // 编辑器不自动被内容撑高
        initialFrameHeight: 500, // 初始容器高度
        initialFrameWidth: "100%", // 初始容器宽度
        UEDITOR_HOME_URL: "/admin/UEditor/",
        serverUrl: "",
      },
      isChoice: "单选",
      modalPic: false,
      modal_loading: false,
      images: [],
      templateList: [
        { id: 0, name: "非付费会员" },
        { id: 1, name: "付费会员" },
      ],
      columns: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "图片",
          slot: "image",
          align: "center",
          minWidth: 120,
        },
        {
          title: "名称",
          align: "center",
          minWidth: 80,
          key: "name",
        },
        {
          title: "奖品",
          slot: "type",
          align: "center",
          minWidth: 80,
        },
        {
          title: "提示语",
          key: "prompt",
          align: "center",
          minWidth: 80,
        },
        {
          title: "数量",
          slot: "total",
          align: "center",
          minWidth: 80,
        },
        {
          title: "奖品权重",
          slot: "chance",
          align: "center",
          minWidth: 80,
        },
        {
          title: "奖品概率",
          key: "probability",
          align: "center",
          minWidth: 80,
        },
        {
          title: "操作",
          slot: "setting",
          align: "center",
          minWidth: 80,
        },
      ],
      specsData: [],
      formValidate: {
        images: [],
        name: "", //活动名称
        desc: "", //活动描述
        image: "", //活动背景图
        factor: "1", //抽奖类型：1:积分 2:余额 3：下单支付成功 4:订单评价',5:关注
        factor_num: 1, //获取一次抽奖的条件数量
        attends_user: 1, //参与用户1：所有  2：部分
        user_level: 0, //参与用户等级
        user_label: [], //参与用户标签
        is_svip: "-1", //参与用户是否付费会员
        prize_num: 0, //奖品数量
        period: [], //活动时间
        prize: [], //奖品数组
        lottery_num_term: 1, //抽奖次数限制：1：每天2：每人
        lottery_num: 1, //抽奖次数
        spread_num: 1, //关注推广获取抽奖次数
        is_all_record: 0, //中奖纪录展示
        is_personal_record: 0, //个人中奖纪录展示
        is_content: 0, //活动规格是否展示
        content: "", //富文本内容
        status: 0, //状态
        total_lottery_num: 1, //每人抽奖总次数
        type: 1, //抽奖类型 1：九宫格 2：大转盘
      },
      ruleValidate: lotteryFrom,
      currentid: "",
      picTit: "",
      tableIndex: 0,
      copy: 0,
      editIndex: null,
      id: "",
      isData: 0,
      content: "",
    };
  },
  filters: {
    typeName(type) {
      if (type == 1) {
        return "未中奖";
      } else if (type == 2) {
        return "积分";
      } else if (type == 3) {
        return "余额";
      } else if (type == 4) {
        return "红包";
      } else if (type == 5) {
        return "优惠券";
      } else if (type == 6) {
        return "商品";
      }
    },
  },
  computed: {
    ...mapState("admin/layout", ["isMobile", "menuCollapse"]),
    labelWidth() {
      return this.isMobile ? undefined : 135;
    },
    labelPosition() {
      return this.isMobile ? "top" : "right";
    },
  },
  mounted() {
    // if (this.$route.query.id) {
    //   this.id = this.$route.query.id;
    //   this.copy = this.$route.query.copy || 0;
    // }
    this.prizeList();
    this.getInfo();
    this.labelListApi();
    this.levelListApi();
    this.setCopyrightShow({ value: false });
  },
  destroyed() {
    this.setCopyrightShow({ value: true });
  },
  methods: {
    ...mapMutations("admin/layout", ["setCopyrightShow"]),
    prizeList() {
      let list = [];
      let data = {
        type: 1, //类型 1：未中奖2：积分  3:余额  4：红包 5:优惠券 6：站内商品
        name: "", //活动名称
        num: 0, //奖品数量
        image: "", //奖品图片
        chance: 1, //中奖权重
        total: 0, //奖品数量
        prompt: "", //提示语
      };
      for (var i = 0; i < 8; i++) {
        list.push(data);
      }
      this.specsData = JSON.parse(JSON.stringify(list));
    },
    getEditorContent(data) {
      this.content = data;
    },
    closeLabel(label) {
      let index = this.dataLabel.indexOf(
        this.dataLabel.filter((d) => d.id == label.id)[0]
      );
      this.dataLabel.splice(index, 1);
    },
    activeData(dataLabel) {
      this.labelShow = false;
      this.dataLabel = dataLabel;
    },
    openLabel(row) {
      this.labelShow = true;
      this.$refs.userLabel.userLabel(
        JSON.parse(JSON.stringify(this.dataLabel))
      );
    },
    // 标签弹窗关闭
    labelClose() {
      this.labelShow = false;
    },
    //用户标签列表
    labelListApi() {
      labelListApi().then((res) => {
        this.userLabelList = res.data.list;
      });
    },
    //用户等级列表
    levelListApi() {
      levelListApi().then((res) => {
        this.userLevelListApi = res.data.list;
      });
    },
    // 具体日期
    onchangeTime(e) {
      this.$nextTick(() => {
        this.$set(this.formValidate, "period", e);
      });
    },
    lotteryType() {
      this.getInfo();
    },
    // 详情
    getInfo() {
      this.spinShow = true;
      lotteryFactorInfo(this.formValidate.factor).then((res) => {
        let factor = this.formValidate.factor;
        this.spinShow = false;
        this.isData = res.data.id || 0;
        if (!this.isData) {
          this.prizeList();
          this.formValidate = {
            factor: factor,
            images: [],
            name: "", //活动名称
            desc: "", //活动描述
            image: "", //活动背景图
            factor_num: 1, //获取一次抽奖的条件数量
            attends_user: 1, //参与用户1：所有  2：部分
            user_level: 0, //参与用户等级
            user_label: [], //参与用户标签
            is_svip: "-1", //参与用户是否付费会员
            prize_num: 0, //奖品数量
            period: [], //活动时间
            prize: [], //奖品数组
            lottery_num_term: 1, //抽奖次数限制：1：每天2：每人
            lottery_num: 1, //抽奖次数
            spread_num: 1, //关注推广获取抽奖次数
            is_all_record: 0, //中奖纪录展示
            is_personal_record: 0, //个人中奖纪录展示
            is_content: 0, //活动规格是否展示
            content: "", //富文本内容
            status: 0, //状态
          };
          return;
        }
        this.formValidate = res.data;
        this.formValidate.factor = res.data.factor.toString();
        this.formValidate.user_level = res.data.user_level || [];
        this.formValidate.user_label = res.data.user_label || [];
        this.dataLabel = res.data.user_label || [];
        this.formValidate.is_svip = res.data.is_svip;
        this.content = res.data.is_content ? res.data.content : "";
        this.formValidate.period = [
          this.formatDate(res.data.start_time) || "",
          this.formatDate(res.data.end_time) || "",
        ];
        this.specsData = res.data.prize;
        this.getProbability();
      });
    },
    // 下一步
    next(name) {
      this.formValidate.prize = this.specsData;
      if (this.formValidate.is_content) {
        this.formValidate.content = formatRichText(this.content);
      }
      if (this.submitOpen) return false;
      this.$refs[name].validate((valid) => {
        if (valid) {
          if (this.formValidate.period[0] == "") {
            return this.$Message.error("请选择日期");
          }
          this.submitOpen = true;
          // 用户标签
          let activeIds = [];
          this.dataLabel.forEach((item) => {
            activeIds.push(item.id);
          });
          this.formValidate.user_label = activeIds;
          if (this.isData) {
            lotteryEditApi(this.isData, this.formValidate)
              .then(async (res) => {
                this.$Message.success(res.msg);
                setTimeout(() => {
                  this.submitOpen = false;
                  // this.$router.push({
                  //   path: "/admin/marketing/lottery/index",
                  // });
                }, 500);
              })
              .catch((res) => {
                this.submitOpen = false;
                this.$Message.error(res.msg);
              });
          } else {
            lotteryCreateApi(this.formValidate)
              .then(async (res) => {
                this.$Message.success(res.msg);
                setTimeout(() => {
                  this.submitOpen = false;
                  // this.$router.push({
                  //   path: "/admin/marketing/lottery/index",
                  // });
                }, 500);
              })
              .catch((res) => {
                this.submitOpen = false;
                this.$Message.error(res.msg);
              });
          }
        } else {
          return this.$Message.error("请完善信息");
        }
      });
    },
    // 上一步
    step() {
      this.current--;
    },
    // 内容
    getContent(val) {
      this.formValidate.content = val;
    },
    // 规则
    getRole(val) {
      this.formValidate.rule = val;
    },
    // 点击商品图
    modalPicTap(tit, picTit, index) {
      this.modalPic = true;
      this.isChoice = tit === "dan" ? "单选" : "多选";
      this.picTit = picTit || "";
      this.tableIndex = index;
    },
    // 获取单张图片信息
    getPic(pc) {
      switch (this.picTit) {
        case "danFrom":
          this.formValidate.image = pc.att_dir;
          break;
        default:
          this.specsData[this.tableIndex].image = pc.att_dir;
      }
      this.modalPic = false;
    },
    handleRemove() {
      this.formValidate.image = "";
    },
    // 表单验证
    // validate(prop, status, error) {
    //   if (status === false) {
    //     this.$Message.error(error);
    //     return false;
    //   } else {
    //     return true;
    //   }
    // },
    // 添加自定义弹窗
    addCustomDialog(editorId) {
      window.UE.registerUI(
        "test-dialog",
        function(editor, uiName) {
          // 创建 dialog
          let dialog = new window.UE.ui.Dialog({
            // 指定弹出层中页面的路径，这里只能支持页面，路径参考常见问题 2
            iframeUrl: "/admin/widget.images/index.html?fodder=dialog",
            // 需要指定当前的编辑器实例
            editor: editor,
            // 指定 dialog 的名字
            name: uiName,
            // dialog 的标题
            title: "上传图片",
            // 指定 dialog 的外围样式
            cssRules: "width:1200px;height:500px;padding:20px;",
          });
          this.dialog = dialog;
          var btn = new window.UE.ui.Button({
            name: "dialog-button",
            title: "上传图片",
            cssRules: `background-image: url(../../../assets/images/icons.png);background-position: -726px -77px;`,
            onclick: function() {
              // 渲染dialog
              dialog.render();
              dialog.open();
            },
          });
          return btn;
        },
        37
      );
    },
    //新增商品
    addGoods() {
      this.addGoodsModel = true;
      this.title = "添加商品";
      this.editData = {};
    },
    //编辑商品
    editGoods(index) {
      this.addGoodsModel = true;
      this.title = "编辑奖品";
      this.editData = this.specsData[index];
      this.editIndex = index;
    },
    //删除商品
    deleteGoods(index) {
      this.specsData.splice(index, 1);
    },
    //获取数组中某个字段之和
    sumArr(arr, name) {
      let arrData = [];
      for (let i = 0; i < arr.length; i++) {
        arrData.push(arr[i][name]);
      }
      return eval(arrData.join("+"));
    },
    addGoodsData(data) {
      this.editIndex != null
        ? this.$set(this.specsData, [this.editIndex], data)
        : this.specsData.length < 8
        ? this.specsData.push(data)
        : this.$Message.warning("最多添加8个奖品");
      this.getProbability();
      this.addGoodsModel = false;
      this.editIndex = null;
    },
    changeChance(data, index) {
      this.$set(this.specsData[index], "chance", data);
      this.$nextTick((e) => {
        this.getProbability();
      });
    },
    changeTotal(data, index) {
      this.$set(this.specsData[index], "total", data);
    },
    //获取商品中奖概率
    getProbability() {
      let sum = 0;
      sum = this.sumArr(this.specsData, "chance");
      for (let j = 0; j < this.specsData.length; j++) {
        this.$set(
          this.specsData[j],
          "probability",
          ((this.specsData[j].chance / sum) * 100).toFixed(2) + "%"
        );
      }
    },
    //修改排序
    onDragDrop(a, b) {
      this.specsData.splice(
        b,
        1,
        ...this.specsData.splice(a, 1, this.specsData[b])
      );
    },
    //时间格式转换
    formatDate(time) {
      if (time) {
        let date = new Date(time * 1000);
        return formatDate(date, "yyyy-MM-dd hh:mm");
      } else {
        return "";
      }
    },
  },
};
</script>

<style scoped lang="stylus">
.labelInput{
	border: 1px solid #dcdee2;
	width 50%;
	padding 0 5px;
	border-radius 5px;
	min-height 30px;
	cursor pointer;
	.span{
		 color: #c5c8ce;
	}
	.iconxiayi{
		font-size 12px
		padding-right 5px;
	}
}
.submit-form-item {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 200px;
  z-index: 99;
  padding: 15px 0;
  margin-bottom: 0;
  background-color: #FFFFFF;
  box-shadow: 0 -1px 2px #f0f0f0;
  text-align: center;
}

.custom-label {
  display: inline-flex;
  line-height: 1.5;
}

.grey {
  color: #999;
}

.maxW /deep/.ivu-select-dropdown {
  max-width: 600px;
}

.ivu-table-wrapper {
  border-left: 1px solid #dcdee2;
  border-top: 1px solid #dcdee2;
}

.tabBox_img {
  width: 50px;
  height: 50px;
}

.tabBox_img img {
  width: 100%;
  height: 100%;
}

.priceBox {
  width: 100%;
}

.form {
  .picBox {
    display: inline-block;
    cursor: pointer;
  }

  .pictrue {
    width: 60px;
    height: 60px;
    border: 1px dotted rgba(0, 0, 0, 0.1);
    margin-right: 15px;
    display: inline-block;
    position: relative;
    cursor: pointer;

    img {
      width: 100%;
      height: 100%;
    }

    .btndel {
      position: absolute;
      z-index: 9;
      width: 20px !important;
      height: 20px !important;
      left: 46px;
      top: -4px;
    }
  }

  .upLoad {
    width: 58px;
    height: 58px;
    line-height: 58px;
    border: 1px dotted rgba(0, 0, 0, 0.1);
    border-radius: 4px;
    background: rgba(0, 0, 0, 0.02);
    cursor: pointer;
  }
}
	.form-submit {
		  /deep/.ivu-card{
		  	border-radius: 0;
		  }
	    margin-bottom: 79px;

	    .fixed-card {
	        position: fixed;
	        right: 0;
	        bottom: 0;
	        left: 200px;
	        z-index: 99;
	        box-shadow: 0 -1px 2px rgb(240, 240, 240);

	        /deep/ .ivu-card-body {
	            padding: 15px 16px 14px;
	        }

	        .ivu-form-item {
	            margin-bottom: 0;
	        }

	        /deep/ .ivu-form-item-content {
	            margin-right: 124px;
	            text-align: center;
	        }

	        .ivu-btn {
	            height: 36px;
	            padding: 0 20px;
	        }
	    }
	}
  .new_tab {
    >>>.ivu-tabs-nav .ivu-tabs-tab{
        padding:4px 16px 20px !important;
        font-weight: 500;
    }
  }
  .input-number-wrapper {
    position: relative;
    display: inline-block;

    .ivu-input-number {
      width: 100%;
    }

    .suffix {
      position: absolute;
      top: 0;
      right: -22px;
      color: #999;
    }
  }
</style>
