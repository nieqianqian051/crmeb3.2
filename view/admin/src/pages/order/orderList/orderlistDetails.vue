<template>
<!-- 订单列表 -->
    <div>
        <Card :bordered="false" dis-hover class="ivu-mt" :padding="0">
            <!-- 订单列表-筛选条件 -->
            <div class="new_card_pd">
                <table-form
                    :is-all="isAll"
                    :auto-disabled="autoDisabled"
                    :form-selection="selection"
                    :orderDataStatus="orderDataStatus"
                    @getList="getData"
                    @order-data="orderDatas"
                    @onChangeType="onChangeType"/>
            </div>
        </Card>
        <cards-data :cardLists="cardLists" v-if="cardLists.length >= 0"></cards-data>
        <Card :bordered="false" dis-hover>
            <!-- 订单列表-表格组件-->
            <table-list
                ref="table"
                :where="orderData"
                :is-all="isAll"
                :currentTab="currentTab"
                @on-all="onAll"
                @auto-disabled="onAutoDisabled"
                @order-data="onOrderData"
                @on-changeCards="getCards"
                @changeGetTabs="changeGetTabs"
                @order-select="orderSelect"
                @selectChange2="selectChange2"

            />
        </Card>
    </div>
</template>

<script>
import cardsData from '../../../components/cards/cards';
import tableForm from './components/tableFrom';
import tableList from './components/tableList';
export default {
    name: 'orderlistDetails',
    components: {
        tableForm,
        tableList,
        cardsData
    },
    data() {
        return {
            currentTab: '-1',
            cardLists: [],
            selection: [],
            orderData: {
                status: '',
                data: '',
                real_name: '',
                field_key: 'all',
                pay_type: ''
            },
            // display: 'none',
            autoDisabled: true,
            orderDataStatus:'',
            isAll: 0
        };
    },
    methods: {
        onChangeType(value) {
          this.currentTab = value;
        },
        changeGetTabs() {
            this.$refs.table.getTabs();
        },
        // 列表数据
        getData(res) {
            if (this.$refs.table) {
                this.$refs.table.checkBox = false;
                this.$refs.table.getList(res);
            }
        },
        // 模块数据
        getCards(list) {
            this.cardLists = list;
        },
        handleResize() {
            this.$refs.ellipsis.forEach(item => item.init());
        },
        orderSelect(selection) {
            this.selection = selection;
        },
        onOrderData(e) {
            this.orderData = e;
        },
        orderDatas(e) {
            this.orderData = e;
        },
        onAutoDisabled(e) {
            this.autoDisabled = e ? true : false;
        },
        onAll (e){
            this.isAll = e
        },
        selectChange2(value) {
          this.orderDataStatus = value;
        }
    },
    mounted() {

    }
};
</script>

<style scoped lang="stylus">
.card_cent >>> .ivu-card-body
    width 100%
    height 100%
.card_box
    width 100%
    height 100%
    display flex
    align-items: center
    justify-content: center
    padding: 25px
    box-sizing: border-box
    border-radius: 4px
    .card_box_img
        width 48px
        height 48px
        border-radius: 50%
        overflow: hidden
        margin-right: 20px
        img
            width 100%
            height 100%
    .card_box_txt
        .sp1
            display block
            color #252631
            font-size 24px
        .sp2
            display block
            color #98A9BC
            font-size 12px
</style>
