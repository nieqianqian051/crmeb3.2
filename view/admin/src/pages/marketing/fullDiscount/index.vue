<template>
<!-- 营销-满减满折 -->
    <div>
        <Card :bordered="false" dis-hover class="ivu-mt" :padding= "0">
            <div class="new_card_pd">
				<!-- 查询条件 -->
			<Form
			  ref="discountFrom"
			  :model="discountFrom"
			  inline
			  :label-width="labelWidth"
			  :label-position="labelPosition"
			  @submit.native.prevent
			>
			      <FormItem label="是否有效：">
			        <Select
			          v-model="discountFrom.status"
			          placeholder="请选择"
			          clearable
			          @on-change="discountSearchs"
					 class="input-add"
			        >
			          <Option value="1">是</Option>
			          <Option value="0">否</Option>
			        </Select>
			      </FormItem>
					  <FormItem label="条件类型：">
					    <Select
					      v-model="discountFrom.threshold_type"
					      placeholder="请选择"
					      clearable
					      @on-change="discountSearchs"
						  class="input-add"
					    >
					      <Option value="1">满N元</Option>
					      <Option value="2">满N件</Option>
					    </Select>
					  </FormItem>
					  <FormItem label="活动名称：">
					    <Input
					      v-model="discountFrom.name"
					      placeholder="请输入活动名称"
					      @on-search="discountSearchs"
						  class="input-add mr14"
					    ></Input>
						<Button type="primary" @click="discountSearchs()">查询</Button>
					  </FormItem>
			</Form>
			</div>
			</Card>
			<Card :bordered="false" dis-hover class="ivu-mt">
				<!-- 操作 -->
				<Button type="primary" @click="add">添加满减满折</Button>
				<!-- 满减满折-表格 -->
            <Table :columns="columns1" :data="list" ref="table" class="ivu-mt"
                   :loading="loading" highlight-row
                   no-userFrom-text="暂无数据"
                   no-filtered-userFrom-text="暂无筛选结果">
                <template slot-scope="{ row, index }" slot="icons">
                    <viewer>
                        <div class="tabBox_img">
                            <img v-lazy="row.icon">
                        </div>
                    </viewer>
                </template>
				<template slot-scope="{ row, index }" slot="threshold_type">
					<div v-if="row.threshold_type == 1">满N元</div>
					<div v-if="row.threshold_type == 2">满N件</div>
				</template>
				<template slot-scope="{ row, index }" slot="status">
					<i-switch v-model="row.status" :true-value="1" :false-value="0" size="large" @on-change="onchangeIsShow(row)">
						<span slot="open">开启</span>
						<span slot="close">关闭</span>
					</i-switch>
				</template>
                <template slot-scope="{ row, index }" slot="action">
                    <a @click="edit(row.id)">编辑</a>
                    <Divider type="vertical" />
                    <a @click="del(row,'删除满减满折',index)">删除</a>
                </template>
            </Table>
            <div class="acea-row row-right page">
                <Page :total="total" show-elevator show-total @on-change="pageChange"
                      :page-size="discountFrom.limit"/>
            </div>
        </Card>
    </div>
</template>

<script>
    import { mapState } from 'vuex';
    import { discountList, discountsetStatus } from '@/api/marketing';
    export default {
        name: "piecesDiscount",
        data() {
            return {
                grid: {
                    xl: 7,
                    lg: 7,
                    md: 12,
                    sm: 24,
                    xs: 24
                },
                loading: false,
                columns1: [
                    {
                        title: 'ID',
                        key: 'id',
                        width: 80
                    },
                    {
                        title: '活动名称',
                        key: 'name',
                        minWidth: 100
                    },
                    {
                      title: '参与商品数',
                      key: 'product_count',
                      minWidth: 100
                    },
										{
												title: '活动条件',
												slot: 'threshold_type',
                        minWidth: 100
										},
										{
												title: '活动详情',
												key: 'desc',
												minWidth: 100
										},
										{
										    title: '支付订单',
										    key: 'sum_order',
										    minWidth: 100
										},
										{
												title: '参与客户',
												key: 'sum_user',
                        minWidth: 100
										},
										{
												title: '实付金额',
												key: 'sum_pay_price',
												minWidth: 100
										},
										{
												title: '是否开启',
												slot: 'status',
                        width: 120
										},
                    {
                        title: '操作',
                        slot: 'action',
                        fixed: 'right',
                        width: 120
                    }
                ],
                discountFrom: {
                    page: 1,
                    limit: 15,
					          name:'',
										status:'',
										threshold_type:''
                },
                list: [],
                total:0
            }
        },
        computed: {
            ...mapState('admin/layout', [
                'isMobile'
            ]),
            labelWidth () {
                return this.isMobile ? undefined : 96;
            },
            labelPosition () {
                return this.isMobile ? 'top' : 'right';
            }
        },
        created () {
            this.getList();
        },
        methods:{
					  // 删除
					  del(row, tit, num) {
					    let delfromData = {
					      title: tit,
					      num: num,
					      url: `marketing/promotions/del/${row.id}`,
					      method: "DELETE",
					      ids: "",
					    };
					    this.$modalSure(delfromData)
					      .then((res) => {
					        this.$Message.success(res.msg);
                  this.list.splice(num, 1);
                  if (!this.list.length) {
                    this.discountFrom.page =
                        this.discountFrom.page == 1 ? 1 : this.discountFrom.page - 1;
                  }
									this.getList();
					      })
					      .catch((res) => {
					        this.$Message.error(res.msg);
					      });
					  },
					  onchangeIsShow (row) {
							discountsetStatus(row.id,row.status).then(res=>{
								this.$Message.success(res.msg);
							}).catch(err=>{
								this.$Message.error(err.msg);
							})
						},
						// 添加
						add () {
						    this.$router.push({ path: "/admin/marketing/discount/add_discount/" + 0 });
						},
						discountSearchs(){
							this.discountFrom.page = 1;
							this.list = [];
							this.getList();
						},
            // 单位列表
            getList () {
                this.loading = true;
                discountList(3,this.discountFrom).then(res => {
                    let data = res.data;
                    this.list = data.list;
                    this.total = data.count;
                    this.loading = false;
                }).catch(err => {
                    this.loading = false;
                    this.$Message.error(err.msg);
                })
            },
            pageChange (index) {
                this.discountFrom.page = index;
                this.getList();
            },
            //修改
            edit(id){
							  this.$router.push({ path: "/admin/marketing/discount/add_discount/" + id });
            }
        }
    }
</script>

<style scoped lang="stylus">

</style>
