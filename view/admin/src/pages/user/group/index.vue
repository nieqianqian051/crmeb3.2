<template>
<!-- 用户-用户分组 -->
    <div>
        <Card :bordered="false" dis-hover class="ivu-mt">
            <!-- 相关操作 -->
            <Row type="flex">
                <Col v-bind="grid">
                    <Button v-auth="['admin-user-group']" type="primary" @click="add">添加分组</Button>
                </Col>
            </Row>
            <!-- 用户分组表格 -->
            <Table :columns="columns1" :data="groupLists" ref="table" class="mt25"
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
                <template slot-scope="{ row, index }" slot="action">
                    <a @click="edit(row.id)">修改</a>
                    <Divider type="vertical" />
                    <a @click="del(row,'删除分组',index)">删除</a>
                </template>
            </Table>
            <div class="acea-row row-right page">
                <Page :total="total" show-elevator show-total @on-change="pageChange"
                      :page-size="groupFrom.limit"/>
            </div>
        </Card>
    </div>
</template>

<script>
    import { mapState } from 'vuex';
    import { userGroupApi, groupAddApi } from '@/api/user';
    export default {
        name: "user_group",
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
                        title: '分组名称',
                        key: 'group_name',
                        minWidth: 600
                    },
                    {
                        title: '操作',
                        slot: 'action',
                        fixed: 'right',
                        minWidth: 120,
                        maxWidth: 140
                    }
                ],
                groupFrom: {
                    page: 1,
                    limit: 10
                },
                groupLists: [],
                total:0
            }
        },
        computed: {
            ...mapState('admin/layout', [
                'isMobile'
            ]),
            labelWidth () {
                return this.isMobile ? undefined : 75;
            },
            labelPosition () {
                return this.isMobile ? 'top' : 'left';
            }
        },
        created () {
            this.getList();
        },
        methods:{
            // 添加
            add () {
                this.$modalForm(groupAddApi(0)).then(() => this.getList());
            },
            // 分组列表
            getList () {
                this.loading = true;
                userGroupApi(this.groupFrom).then(async res => {
                    let data = res.data;
                    this.groupLists = data.list;
                    this.total = data.count;
                    this.loading = false;
                }).catch(res => {
                    this.loading = false;
                    this.$Message.error(res.msg);
                })
            },
            pageChange (index) {
                this.groupFrom.page = index;
                this.getList();
            },
            //修改
            edit(id){
                this.$modalForm(groupAddApi(id)).then(() => this.getList());
            },
            // 删除
            del (row, tit, num) {
                let delfromData = {
                    title: tit,
                    num: num,
                    url: `user/user_group/del/${row.id}`,
                    method: 'DELETE',
                    ids: ''
                };
                this.$modalSure(delfromData).then((res) => {
                    this.$Message.success(res.msg);
                    this.groupLists.splice(num, 1);
                    if (!this.groupLists.length) {
                      this.groupFrom.page =
                          this.groupFrom.page == 1 ? 1 : this.groupFrom.page - 1;
                    }
                    this.getList();
                }).catch(res => {
                    this.$Message.error(res.msg);
                });
            },
        }
    }
</script>