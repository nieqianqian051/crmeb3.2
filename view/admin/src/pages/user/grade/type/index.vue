<template>
<!-- 用户-付费会员-会员类型 -->
  <div>
    <Card :bordered="false" dis-hover class="ivu-mt">
      <Button type="primary" @click="addType">添加类型</Button>
      <!-- 会员类型表格 -->
      <Table
        class="mt25"
        :columns="thead"
        :data="tbody"
        :loading="loading"
        highlight-row
        no-userFrom-text="暂无数据"
        no-filtered-userFrom-text="暂无筛选结果"
      >
        <template slot-scope="{ row }" slot="is_del">
          <i-switch
            v-model="row.is_del"
            :value="row.is_del"
            :true-value="0"
            :false-value="1"
            @on-change="onchangeIsShow(row)"
            size="large"
          >
            <span slot="open">启用</span>
            <span slot="close">禁用</span>
          </i-switch>
        </template>
        <template slot-scope="{ row, index }" slot="action">
          <a href="javascript:" @click="editType(row)">编辑</a>
          <Divider
            type="vertical"
            v-if="row.type !== 'free' && row.type !== 'ever'"
          />
          <a
            v-if="row.type !== 'free' && row.type !== 'ever'"
            href="javascript:"
            @click="del(row, '删除类型', index)"
            >删除</a
          >
        </template>
      </Table>
    </Card>
    <Modal
      v-model="modal"
      :title="`${rowModelType}${rowEdit && rowEdit.title}会员`"
      footer-hide
      @on-cancel="cancel"
    >
      <form-create
        v-model="fapi"
        :rule="rule"
        @on-submit="onSubmit"
        @change="change"
      ></form-create>
    </Modal>
  </div>
</template>

<script>
import {
  userMemberShip,
  memberShipSave,
  memberCard,
  deleteCard,
} from "@/api/user";

export default {
  name: "list",
  data() {
    return {
      thead: [
        {
          title: "ID",
          key: "id",
          maxWidth: 60
        },
        {
          title: "会员名",
          key: "title",
        },
        {
          title: "有限期（天）",
          key: "vip_day",
          render: (h, params) => {
            return h(
              "span",
              params.row.vip_day === -1 ? "永久" : params.row.vip_day
            );
          },
        },
        {
          title: "划线价",
          key: "price",
        },
        {
          title: "优惠价",
          key: "pre_price",
        },
        {
          title: "排序",
          key: "sort",
        },
        {
          title: "是否开启",
          slot: "is_del",
        },
        {
          title: "操作",
          slot: "action",
        },
      ],
      tbody: [],
      loading: false,
      modal: false,
      rowEdit: {},
      rowModelType: "编辑",
      rule: [
        {
          type: "hidden",
          field: "id",
          value: "",
        },
        {
          type: "hidden",
          field: "type",
          value: "",
        },
        {
          type: "input",
          field: "title",
          title: "会员名",
          value: "",
          props: {
            disabled: false,
            required: true
          },
          validate: [
            {
              type: "string",
              max: 5,
              min: 1,
              message: "请输入长度为1-5的名称",
              requred: true,
            },
          ],
        },
        {
          type: "InputNumber",
          field: "vip_day",
          title: "有限期（天）",
          value: null,
          props: {
            precision: 0,
            disabled: false,
            type: "text",
            required: true
          },
          validate: [
            {
              type: "number",
              max: 1000000,
              min: 0,
              message: "最大只能输入1000000,最小为0",
              requred: true,
            },
          ],
        },
        {
          type: "InputNumber",
          field: "price",
          title: "划线价",
          value: null,
          props: {
            min: 0,
            disabled: false,
            required: true
          },
          validate: [
            {
              type: "number",
              max: 1000000,
              min: 0,
              message: "最大只能输入1000000,最小为0",
              requred: true,
            },
          ],
        },
        {
          type: "InputNumber",
          field: "pre_price",
          title: "优惠价",
          value: null,
          props: {
            min: 0,
            disabled: false,
            required: true
          },
          validate: [
            {
              type: "number",
              max: 1000000,
              min: 0.01,
              message: "最大只能输入1000000,最小为0.01",
              requred: true,
            },
          ],
        },
        {
          type: "InputNumber",
          field: "sort",
          title: "排序",
          value: 0,
          props: {
            precision: 0,
            min: 1,
            max: 1000000,
            disabled: false,
          },
          validate: [
            {
              type: "number",
              max: 1000000,
              min: 0,
              message: "最大只能输入1000000,最小为0",
              requred: true,
            },
          ],
        },
        {
          type: "radio",
          field: "is_label",
          title: "特惠标签",
          value: 0,
          options:[
              {value:1,label:"是"},
              {value:0,label:"否"},
          ],
        },
      ],
      fapi: {
        id: "",
        pre_price: null,
        price: null,
        sort: null,
        title: "",
        type: "owner",
        vip_day: null,
      },
    };
  },
  created() {
    this.getMemberShip();
  },
  mounted() {},
  methods: {
    // 监听form-create变化
    change(field, value) {
      this.$nextTick(() => {
        if (['pre_price', 'price'].includes(field)) {
          let string = Number(value).toString()
          let index = string.indexOf('.')
          if (index > -1) {
            if (string.length - index - 1 > 2) {
              this.fapi.bind()[field] = Number(Number(string).toFixed(2))
            }
          }
        }
      })
    },
    onchangeIsShow(row) {
      let data = {
        id: row.id,
        is_del: row.is_del,
      };
      memberCard(data)
        .then((res) => {
          this.$Message.success(res.msg);
          this.getMemberShip();
        })
        .catch((err) => {
          this.$Message.error(err.msg);
        });
    },
    cancel() {
      this.fapi.resetFields();
    },
    getMemberShip() {
      this.loading = true;
      userMemberShip()
        .then((res) => {
          this.loading = false;
          const { count, list } = res.data;
          this.total = count;
          this.tbody = list;
        })
        .catch((err) => {
          this.loading = false;
          this.$Message.error(err.msg);
        });
    },
    addType() {
      this.rowEdit.id = 0;
      this.rowModelType = "新增";
      this.rule[1].value = "owner";
      this.rule[3].props.disabled = false;
      this.rule[5].props.disabled = false;
      this.rowEdit.title = "";
      // this.cancel();
      this.modal = true;
    },
    del(row, tit, num) {
      let delfromData = {
        title: tit,
        num: num,
        url: `user/member_ship/delete/${row.id}`,
        method: "DELETE",
        ids: "",
      };
      this.$modalSure(delfromData)
        .then((res) => {
          this.$Message.success(res.msg);
          this.getMemberShip();
        })
        .catch((res) => {
          this.$Message.error(res.msg);
        });
    },
    editType(row) {
      this.rule.forEach((item) => {
        for (const key in row) {
          if (row.hasOwnProperty(key)) {
            if (item.field === key) {
              if (key === "vip_day") {
                if (row[key] === -1 || row[key] == "永久") {
                  item.type = "Input";
                  item.props.disabled = true;
    							item.validate = [
    							  { type: "string", message: "", requred: true },
    							];
									row[key] = "永久";
                } else {
									item.type = "InputNumber";
                  item.props.disabled = false;
                  item.props.min = 1;
                  item.validate = [
                    {
                      type: "number",
                      max: 1000000,
                      min: 0,
                      message: "最大只能输入1000000,最小为0",
                      requred: true,
                    },
                  ];
                }
              }

    					if (["price"].includes(key)) {
                row[key] = parseFloat(row[key]);
              }
              if (["pre_price"].includes(key)) {
                row[key] = parseFloat(row[key]);
                if (row.type == 'free') {
                  item.props.disabled = true;
                  item.validate = [];
                } else {
                  item.props.disabled = false;
                }
              }
							item.value = row[key];
            }
          }
        }
      });
      this.rowModelType = "编辑";
      this.rowEdit = JSON.parse(JSON.stringify(row));
      this.modal = true;
    },
    onSubmit(formData) {
      memberShipSave(this.rowEdit.id, formData)
        .then((res) => {
          this.modal = false;
          this.$Message.success(res.msg);
          this.getMemberShip();
          this.cancel();
        })
        .catch((err) => {
          this.$Message.error(err.msg);
        });
    },
  },
};
</script>
<style scoped lang="stylus">
/deep/ .ivu-modal {
  top: 20% !important;
}
/deep/ .ivu-input {
  width: 80px;
}
</style>
