<template>
<!-- 运营-首页-用户管理组件 -->
    <Row :gutter="24" class="dashboard-console-grid">
        <Col v-bind="grid" class="ivu-mb">
            <Card :bordered="false">
                <router-link :to="{path:'/admin/user/list'}">
                    <Icon type="md-people" color="#69c0ff" />
                    <p>用户管理</p>
                </router-link>
            </Card>
        </Col>
        <Col v-bind="grid" class="ivu-mb">
            <Card :bordered="false">
                <router-link :to="{path:'/admin/setting/shop/base'}">
                    <Icon type="md-settings" color="#95de64" />
                    <p>系统设置</p>
                </router-link>
            </Card>
            
        </Col>
        <Col v-bind="grid" class="ivu-mb">
            <Card :bordered="false">
                <router-link :to="{path:'/admin/product/product_list'}">
                    <Icon type="md-cart" color="#ff9c6e" />
                    <p>商品</p>
                </router-link>
            </Card>
        </Col>
        <Col v-bind="grid" class="ivu-mb">
            <Card :bordered="false">
                <router-link :to="{path:'/admin/order/list'}">
                    <Icon type="md-clipboard" color="#b37feb" />
                    <p>订单管理</p>
                </router-link>
            </Card>
        </Col>
        <Col v-bind="grid" class="ivu-mb">
            <Card :bordered="false">
                <router-link :to="{path:'/admin/setting/notification/index'}">
                    <Icon type="md-chatboxes" color="#ffd666" />
                    <p>消息配置</p>
                </router-link>
            </Card>
        </Col>
        <Col v-bind="grid" class="ivu-mb">
            <Card :bordered="false">
                <router-link :to="{path:'/admin/content/article/index'}">
                    <Icon type="md-card" color="#5cdbd3" />
                    <p>文章管理</p>
                </router-link>
            </Card>
        </Col>
        <Col v-bind="grid" class="ivu-mb">
            <Card :bordered="false">
                <router-link :to="{path:'/admin/agent/agent_manage/index'}">
                    <Icon type="md-pricetags" color="#ff85c0" />
                    <p>分销管理</p>
                </router-link>
            </Card>
        </Col>
        <Col v-bind="grid" class="ivu-mb">
            <Card :bordered="false">
                <router-link :to="{path:'/admin/marketing/store_coupon_issue/index'}">
                    <Icon type="md-cash" color="#ffc069" />
                    <p>优惠券</p>
                </router-link>
            </Card>
        </Col>
    </Row>
</template>
<script>
    export default {
        data () {
            return {
                grid: {
                    xl: 3,
                    lg: 6,
                    md: 6,
                    sm: 8,
                    xs: 8
                }
            }
        }
    }
</script>
<style lang="less">
    .dashboard-console-grid{
        text-align: center;
        .ivu-card-body{
            padding: 0;
        }
        i{
            font-size: 32px;
        }
        a{
            display: block;
            color: inherit;
            padding: 16px;
        }
        p{
            margin-top: 8px;
        }
    }
</style>
