<template>
  <div>
    <!-- <div class="i-layout-page-header">
        <PageHeader class="product_tabs" :title="$route.meta.title" hidden-breadcrumb></PageHeader>
    </div> -->
    <Card :bordered="false" :padding="30" dis-hover>
      <Form ref="levelFrom" inline :model="formValidate"  :label-width="labelWidth" :label-position="labelPosition" @submit.native.prevent>
        <FormItem label="回复类型：" prop="type" label-for="type">
          <Select v-model="formValidate.type" placeholder="请选择" element-id="type" class="input-add" clearable @on-change="userSearchs">
            <Option value="text">文字消息</Option>
            <Option value="image">图片消息</Option>
            <Option value="news">图文消息</Option>
            <Option value="voice">声音消息</Option>
          </Select>
        </FormItem>
        <FormItem label="关键字：" prop="key" label-for="key">
          <Input v-model="formValidate.key" placeholder="请输入关键字" @on-search="userSearchs"  class="input-add"/>
          <Button type="primary" @click="userSearchs">查询</Button>
        </FormItem>
      </Form>
      <Button type="primary" @click="add">添加关键字</Button>
      <Table :columns="columns1" :data="tabList" ref="table" class="mt25"
             :loading="loading" highlight-row
             no-userFrom-text="暂无数据"
             no-filtered-userFrom-text="暂无筛选结果">
        <template slot-scope="{ row, index }" slot="status">
          <i-switch v-model="row.status" :value="row.status" :true-value="1" :false-value="0" @on-change="onchangeIsShow(row)" size="large">
            <span slot="open">显示</span>
            <span slot="close">隐藏</span>
          </i-switch>
        </template>
        <template slot-scope="{ row, index }" slot="action">
          <a @click="edit(row)">编辑</a>
          <Divider type="vertical" />
          <a @click="download(row)">下载二维码</a>
          <Divider type="vertical" />
          <a @click="del(row, '关键字回复', index)">删除</a>
        </template>
      </Table>
      <div class="acea-row row-right page">
        <Page :total="total" :current="formValidate.page" show-elevator show-total @on-change="pageChange"
              :page-size="formValidate.limit"/>
      </div>
    </Card>
    <Modal v-model="modal" title="二维码" footer-hide>
      <div class="acea-row row-around">
        <div class="acea-row row-column-around row-between-wrapper">
          <div v-viewer class="QRpic">
            <img v-lazy="qrcode" />
          </div>
        </div>
      </div>
    </Modal>
  </div>
</template>

<script>
import { keywordListApi, keywordsetStatusApi, downloadReplyCode } from '@/api/app';
import { mapState } from 'vuex';
export default {
  name: 'keyword',
  data () {
    return {
      grid: {
        xl: 7,
        lg: 7,
        md: 12,
        sm: 24,
        xs: 24
      },
      loading: false,
      formValidate: {
        key: '',
        type: '',
        page: 1,
        limit: 20
      },
      tabList: [],
      total: 0,
      columns1: [
        {
          title: 'ID',
          key: 'id',
          width: 80
        },
        {
          title: '关键字',
          key: 'key',
          minWidth: 120
        },
        {
          title: '回复类型',
          key: 'typeName',
          minWidth: 150
        },
        {
          title: '是否显示',
          slot: 'status',
          minWidth: 120
        },
        {
          title: '操作',
          slot: 'action',
          fixed: 'right',
          width: 200
        }
      ],
      modal: false,
      qrcode: ''
    }
  },
  created () {
    this.getList();
  },
  computed: {
    ...mapState('admin/layout', [
      'isMobile'
    ]),
    labelWidth () {
      return this.isMobile ? undefined : 75;
    },
    labelPosition () {
      return this.isMobile ? 'top' : 'right';
    }
  },
  methods: {
    // 下载二维码
    download (item) {
      this.$Spin.show();
      downloadReplyCode(item.id).then(res => {
        this.$Spin.hide();
        this.modal = true;
        this.qrcode = res.data.url;
      }).catch(err => {
        this.$Spin.hide();
        this.$Message.error(err.msg);
      });
    },
    // 列表
    getList () {
      this.loading = true;
      keywordListApi(this.formValidate).then(async res => {
        let data = res.data
        this.tabList = data.list;
        this.total = res.data.count;
        this.loading = false;
      }).catch(res => {
        this.loading = false;
        this.$Message.error(res.msg);
      })
    },
    pageChange (index) {
      this.formValidate.page = index;
      this.getList();
    },
    // 修改是否显示
    onchangeIsShow (row) {
      let data = {
        id: row.id,
        status: row.status
      }
      keywordsetStatusApi(data).then(async res => {
        this.$Message.success(res.msg);
      }).catch(res => {
        this.$Message.error(res.msg);
      })
    },
    // 表格搜索
    userSearchs () {
      this.formValidate.page = 1;
      this.getList();
    },
    // 添加
    add () {
      // this.$router.push({ path: '/app/wechat/reply/keyword/save/0' });
      this.$emit('keyword-action', '0');
    },
    // 编辑
    edit (row) {
      // this.$router.push({ path: '/app/wechat/reply/keyword/save/' + row.id });
      this.$emit('keyword-action', `${row.id}`);
    },
    del (row, tit, num) {
      let delfromData = {
        title: tit,
        num: num,
        url: `app/wechat/keyword/${row.id}`,
        method: 'DELETE',
        ids: ''
      }
      this.$modalSure(delfromData).then((res) => {
        this.$Message.success(res.msg);
        this.tabList.splice(num, 1);
        if (!this.tabList.length) {
          this.formValidate.page =
              this.formValidate.page == 1 ? 1 : this.formValidate.page - 1;
        }
        this.getList();
      }).catch(res => {
        this.$Message.error(res.msg);
      });
    }
  }
}
</script>

<style scoped>
.input-add {
  width:250px;
  margin-right:14px
}
.QRpic {
  width: 180px;
  height: 180px;
}

.QRpic img {
  width: 100%;
  height: 100%;
}
</style>
