<template>
<!-- 企业微信-客户群群发 -->
	<div>
		<Card :bordered="false" dis-hover class="ivu-mt" :padding="0">
			<div class="new_card_pd">
				<Form
				ref="tableFrom"
				:label-width="96"
				inline
				:label-colon="true"
				@submit.native.prevent>
					<FormItem label="发送时间">
						<DatePicker
						:editable="false"
						@on-change="onchangeTime"
						:value="timeVal"
						format="yyyy/MM/dd HH:mm"
						type="datetimerange"
						placement="bottom-start"
						placeholder="自定义时间"
						class="input-add"
						:options="options"></DatePicker>
					</FormItem>
          <FormItem label="搜索" label-for="name">
						<Input
							placeholder="请输入群发名称"
							v-model="tableFrom.name"
							@on-search="search"
							class="input-add btn-add"
						/>
						<Button type="primary" @click="search()" class="btn-add">查询</Button>
					</FormItem>
				</Form>
			</div>
		</Card>
		<Card :bordered="false" dis-hover class="ivu-mt">
			<router-link :to="'/admin/work/group/add_template'">
			  	<Button type="primary" class="mr-12">新建群发</Button>
			</router-link>
			<Table ref="selection" :columns="columns1" :data="tableData.list" :loading="loading" class="ivu-mt">
				<template slot-scope="{ row }" slot="send_type">
					<Tag color="green" size="medium" v-if="row.send_type == 1">已发送</Tag>
					<Tag color="red" size="medium" v-if="row.send_type == 0">未发送</Tag>
					<Tooltip max-width="200" placement="bottom">
						<Tag color="orange" size="medium" v-if="row.send_type == -1">未发送成功</Tag>
						<p slot="content">{{row.fail_message}}</p>
					</Tooltip>
				</template>
				<template slot-scope="{ row }" slot="template_type">
					<Tag color="cyan" size="medium" v-if="row.template_type">定时发送</Tag>
					<Tag color="blue" size="medium" v-else>立即发送</Tag>
				</template>
				<template slot-scope="{ row, index }" slot="action">
					<a @click="sendMessage(row, index)" :disabled="row.send_type == 1">提醒发送</a>
					<Divider type="vertical" />
					<a @click="detailsItem(row,index)">详情</a>
					<Divider type="vertical" />
					<a @click="delItem(row,index)">删除</a>
				</template>
			</Table>
			<div class="acea-row row-right page">
			<Page
				:total="tableData.count"
				:current="tableFrom.page"
				show-elevator
				show-total
				@on-change="pageChange"
				:page-size="tableFrom.limit"
			/>
			</div>
		</Card>
	</div>
</template>
<script>
	import { mapState } from "vuex";
	import timeOptions from "@/utils/timeOptions";
	import {getGroupTemplateChatList,workGroupTemplateSendMsg} from "@/api/work";
	import { log } from 'util';

	export default {
		data() {
			return {
				options: timeOptions,
				timeVal: [],
				tableFrom: {
					name:"",
					create_time:"",
					client_type:"",
					page: 1,
					type: '1',
					limit: 15,
				},
				total: 0,
				tableList: [],
				loading: false,
				tableData: [],
				columns1: [
                    {
                        title: '群发名称',
                        key: 'name',
                        minWidth: 150
                    },
                    {
                        title: '已发送群主',
                        key: 'user_count',
                        minWidth: 120
                    },
                    {
                        title: '送达群聊',
                        key: 'external_user_count',
                        minWidth: 120
                    },
                    {
                        title: '未发送群主',
                        key: 'unuser_count',
                        minWidth: 120
                    },
                    {
                        title: '未送达群聊',
                        key: 'external_unuser_count',
                        minWidth: 120
					},
					{
                        title: '是否发送',
                        slot: 'send_type',
                        minWidth: 120
                    },
					{
                        title: '群发类型',
                        slot: 'template_type',
                        minWidth: 120
                    },
                    {
                        title: '发送时间',
                        key: 'update_time',
                        minWidth: 150
                    },
					{
                        title: '创建时间',
                        key: 'create_time',
                        minWidth: 150
                    },
                    {
                        title: '操作',
                        slot: 'action',
                        fixed: 'right',
                        minWidth: 170
                    }
                ],

			}
		},
		computed: {
		  ...mapState("admin/layout", ["isMobile"]),
		  labelWidth() {
		    return this.isMobile ? undefined : 96;
		  },
		  labelPosition() {
		    return this.isMobile ? "top" : "right";
		  }
		},
		created() {
			this.getList();
		},
		methods: {
			getList(){
				this.loading = true;
				getGroupTemplateChatList(this.tableFrom).then(res=>{
					this.tableData = res.data;
					this.loading = false;
				}).catch(err=>{
					this.$Message.error(err.msg);
					this.loading = false;
				})
			},
			search(){
				this.tableFrom.page = 1;
				this.getList();
			},
			// 具体日期
            onchangeTime (e) {
                this.timeVal = e
				this.tableFrom.time = this.timeVal.join("-");
                this.tableFrom.page = 1;
				this.getList();
            },
			pageChange(index){
				this.tableFrom.page = index;
				this.getList();
			},
			// 删除
			delItem(row,index){
				let delfromData = {
					title: '删除该客户群发',
					num:index,
					url: `work/group_template_chat/${row.id}`,
					method: "DELETE",
					ids: "",
				};
				this.$modalSure(delfromData)
					.then((res) => {
					this.$Message.success(res.msg);
          this.tableData.list.splice(index, 1);
          if (!this.tableData.list.length) {
            this.tableFrom.page =
                this.tableFrom.page == 1 ? 1 : this.tableFrom.page - 1;
            }
          this.getList();
					})
					.catch((res) => {
					this.$Message.error(res.msg);
				});
			},
			// 详情
			detailsItem(row,index) {
				this.$router.push("/admin/work/group/template_info/"+ row.id)
			},
			// 提醒发送
			sendMessage(row,index) {
				workGroupTemplateSendMsg({
					userid:"",
					time:row.update_time,
					id:row.id
				}).then(res=>{
					this.$Message.success(res.msg)
				}).catch(err=>{
					this.$Message.error(err.msg);
				})
			}
		}
	}
</script>
<style lang="less" scoped>
	.btn-add {
	margin-right:14px
	}
    .input-add {
	width: 250px;
	}
</style>
