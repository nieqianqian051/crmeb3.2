<template>
<!-- 用户-会员管理-等级列表 -->
    <div>
       <Card :bordered="false" dis-hover class="ivu-mt" :padding="0">
            <div class="new_card_pd">
                <!-- 筛选条件 -->
                 <Form ref="levelFrom" :model="levelFrom" inline :label-width="labelWidth" :label-position="labelPosition" @submit.native.prevent>
                    <FormItem label="状态："  label-for="status1">
                        <Select v-model="levelFrom.is_show" placeholder="请选择" clearable element-id="status1" style="width:250px;">
                            <Option value="1">显示</Option>
                            <Option value="0">不显示</Option>
                        </Select>
                    </FormItem>
                    <FormItem label="等级名称："  label-for="title">
                        <Input  v-model="levelFrom.title"  placeholder="请输入等级名称" style="width:250px;margin-right:14px;"/>
                        <Button type="primary" @click="userSearchs">查询</Button>
                    </FormItem>
                </Form>
            </div>
        </Card>
        <Card :bordered="false" dis-hover class="ivu-mt">
            <!-- 相关操作 -->
            <Button v-auth="['admin-user-level_add']" type="primary" @click="add">添加会员等级</Button>
            <!-- 等级列表表格 -->
            <Table :columns="columns1" :data="levelLists" ref="table" class="mt25"
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
                <template slot-scope="{ row, index }" slot="is_forevers">
                    <i-switch v-model="row.is_forever" :value="row.is_forever" :true-value="1" :false-value="0" :disabled="true" size="large">
                        <span slot="open">永久</span>
                        <span slot="close">非永久</span>
                    </i-switch>
                </template>
                <template slot-scope="{ row, index }" slot="is_pays">
                    <i-switch v-model="row.is_pay" :value="row.is_pay" :true-value="1" :false-value="0"  :disabled="true" size="large">
                        <span slot="open">付费</span>
                        <span slot="close">免费</span>
                    </i-switch>
                </template>
                <template slot-scope="{ row, index }" slot="is_shows">
                    <i-switch v-model="row.is_show" :value="row.is_show" :true-value="1" :false-value="0" @on-change="onchangeIsShow(row)" size="large">
                        <span slot="open">显示</span>
                        <span slot="close">隐藏</span>
                    </i-switch>
                </template>
                <template slot-scope="{ row, index }" slot="action">
                    <a @click="edit(row)">编辑</a>
                    <Divider type="vertical" />
                    <a @click="changeMenu(row,2,index)">删除</a>
<!--                    <template>-->
<!--                        <Dropdown @on-click="changeMenu(row,$event,index)">-->
<!--                            <a href="javascript:void(0)">-->
<!--                                更多-->
<!--                                <Icon type="ios-arrow-down"></Icon>-->
<!--                            </a>-->
<!--                            <DropdownMenu slot="list">-->
<!--&lt;!&ndash;                                <DropdownItem name="1">等级任务</DropdownItem>&ndash;&gt;-->
<!--                                <DropdownItem name="2">删除等级</DropdownItem>-->
<!--                            </DropdownMenu>-->
<!--                        </Dropdown>-->
<!--                    </template>-->
                </template>
            </Table>
            <div class="acea-row row-right page">
                <Page :total="total" :current="levelFrom.page" show-elevator show-total @on-change="pageChange"
                      :page-size="levelFrom.limit"/>
            </div>
        </Card>
        <!-- 等级任务-->
        <task-list ref="tasks"></task-list>
    </div>
</template>
<script>
    import { mapState, mapMutations } from 'vuex';
    import { levelListApi, setShowApi, createApi } from '@/api/user';
    import taskList from './handle/task';
    import editFrom from '@/components/from/from';
    export default {
        name: 'user_level',
        components: { editFrom, taskList },
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
                columns1: [
                    {
                        title: 'ID',
                        key: 'id',
                        width: 80
                    },
                    {
                        title: '等级图标',
                        slot: 'icons',
                        minWidth: 100
                    },
                    {
                        title: '等级名称',
                        key: 'name',
                        minWidth: 120
                    },
                    {
                        title: '等级',
                        key: 'grade',
                        minWidth: 100
                    },
                    {
                        title: '享受折扣(%)',
                        key: 'discount',
                        minWidth: 100
                    },
                    {
                        title: '解锁经验值',
                        key: 'exp_num',
                        minWidth: 120
                    },
                    // {
                    //     title: '是否永久',
                    //     slot: 'is_forevers',
                    //     minWidth: 130
                    // },
                    // {
                    //     title: '是否付费',
                    //     slot: 'is_pays',
                    //     minWidth: 120
                    // },
                    {
                        title: '是否显示',
                        slot: 'is_shows',
                        minWidth: 120
                    },
                    {
                        title: '等级说明',
                        key: 'explain',
                        minWidth: 120
                    },
                    {
                        title: '操作',
                        slot: 'action',
                        fixed: 'right',
                        width: 120
                    }
                ],
                levelFrom: {
                    is_show: '',
                    title: '',
                    page: 1,
                    limit: 10
                },
                levelLists: [],
                total: 0,
                FromData: null,
                imgName: '',
                visible: false,
                levelId: 0,
                modalTitleSs: '',
                titleType: 'level',
                modelTask: false,
                num: 0
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
                return this.isMobile ? undefined : 96;
            },
            labelPosition () {
                return this.isMobile ? 'top' : 'right';
            }
        },
        methods: {
            ...mapMutations('admin/userLevel', [
                'getlevelId'
            ]),
            // 操作
            changeMenu (row, name, num) {
                this.levelId = row.id;
                switch (name) {
                case '1':
                    this.getlevelId(this.levelId);
                    this.$refs.tasks.modals = true;
                    this.$refs.tasks.getList();
                    break;
                default:
                    this.del(row, '删除等级', num);
                }
            },
            // 删除
            del (row, tit, num) {
                let delfromData = {
                    title: tit,
                    num: num,
                    url: `user/user_level/delete/${row.id}`,
                    method: 'put',
                    ids: ''
                }
                this.$modalSure(delfromData).then((res) => {
                    this.$Message.success(res.msg);
                    this.levelLists.splice(num, 1);
                    if (!this.levelLists.length) {
                      this.levelFrom.page =
                          this.levelFrom.page == 1 ? 1 : this.levelFrom.page - 1;
                    }
                    this.getList();
                }).catch(res => {
                    this.$Message.error(res.msg);
                });
            },
            // 删除成功
            // submitModel () {
            //     this.levelLists.splice(this.delfromData.num, 1)
            // },
            // 修改是否显示
            onchangeIsShow (row) {
                let data = {
                    id: row.id,
                    is_show: row.is_show
                }
                setShowApi(data).then(async res => {
                    this.$Message.success(res.msg);
                }).catch(res => {
                    this.$Message.error(res.msg);
                })
            },
            // 等级列表
            getList () {
                this.loading = true;
                this.levelFrom.is_show = this.levelFrom.is_show || '';
                levelListApi(this.levelFrom).then(async res => {
                    let data = res.data
                    this.levelLists = data.list;
                    this.total = res.data.count;
                    this.loading = false;
                }).catch(res => {
                    this.loading = false;
                    this.$Message.error(res.msg);
                })
            },
            pageChange (index) {
                this.levelFrom.page = index;
                this.getList();
            },
            // 添加
            add () {
                this.levelId = 0;
                this.$modalForm(createApi({ id: this.levelId })).then(() => this.getList());
            },
            // 编辑
            edit (row) {
                this.levelId = row.id;
                this.$modalForm(createApi({ id: this.levelId })).then(() => this.getList());
                this.getlevelId(this.levelId);
            },
            // 表格搜索
            userSearchs () {
                this.levelFrom.page = 1;
                this.getList();
            },
            // 修改成功
            submitFail () {
                this.getList();
            }
        }
    }
</script>

<style scoped lang="stylus">
    .tabBox_img
        width 36px
        height 36px
        border-radius:4px
        cursor pointer
        img
            width 100%
            height 100%
</style>
