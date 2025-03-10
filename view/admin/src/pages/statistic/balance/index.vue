<template>
  <div>
    <Card :bordered="false" dis-hover class="ivu-mt new_card_pd" :padding="0">
      <Form ref="formInline"
        :label-width="labelWidth"
        :label-position="labelPosition"
        inline>
        <FormItem label="选择时间：">
          <DatePicker
            :editable="false"
            :clearable="false"
            @on-change="onchangeTime"
            :value="timeVal"
            format="yyyy/MM/dd"
            type="datetimerange"
            placement="bottom-start"
            placeholder="请选择时间"
            style="width: 200px"
            :options="options"
            class="mr20"
          ></DatePicker>
        </FormItem>
      </Form>
    </Card>
    <cards-data
      :cardLists="cardLists"
      v-if="cardLists.length >= 0"
    ></cards-data>
    <Card :bordered="false" dis-hover>
      <h3>余额使用趋势</h3>
      <echarts-new
        :option-data="optionData"
        :styles="style"
        height="100%"
        width="100%"
        v-if="optionData"
      ></echarts-new>
    </Card>
    <Spin size="large" fix v-if="spinShow"></Spin>
    <div class="code-row-bg">
      <Card :bordered="false" dis-hover class="ivu-mt">
        <div class="acea-row row-between-wrapper">
          <h3 class="header-title">余额来源分析</h3>
          <div class="change-style" @click="echartLeft = !echartLeft">
            切换样式
          </div>
        </div>
        <div class="ech-box">
          <echarts-from
            v-if="echartLeft"
            ref="visitChart"
            :infoList="infoList"
            echartsTitle="circle"
          ></echarts-from>
          <Table
            v-show="!echartLeft"
            ref="selection"
            :columns="columns"
            :data="tabList"
            :loading="loading"
            no-data-text="暂无数据"
            highlight-row
            no-filtered-data-text="暂无筛选结果"
          >
            <template slot-scope="{ row }" slot="percent">
              <div class="percent-box">
                <div class="line">
                  <div class="bg"></div>
                  <div
                    class="percent"
                    :style="'width:' + row.percent + '%;'"
                  ></div>
                </div>
                <div class="num">{{ row.percent }}%</div>
              </div>
            </template>
          </Table>
        </div>
      </Card>
      <Card :bordered="false" dis-hover class="ivu-mt">
        <div class="acea-row row-between-wrapper">
          <h3 class="header-title">余额消耗</h3>
          <div class="change-style" @click="echartRight = !echartRight">
            切换样式
          </div>
        </div>
        <div class="ech-box">
          <echarts-from
            v-if="echartRight"
            ref="visitChart"
            :infoList="infoList2"
            echartsTitle="circle"
          ></echarts-from>
          <Table
            v-show="!echartRight"
            ref="selection"
            :columns="columns"
            :data="tabList2"
            :loading="loading2"
            no-data-text="暂无数据"
            highlight-row
            no-filtered-data-text="暂无筛选结果"
          >
            <template slot-scope="{ row }" slot="percent">
              <div class="percent-box">
                <div class="line">
                  <div class="bg"></div>
                  <div
                    class="percent"
                    :style="'width:' + row.percent + '%;'"
                  ></div>
                </div>
                <div class="num">{{ row.percent }}%</div>
              </div>
            </template>
          </Table>
        </div>
      </Card>
    </div>
  </div>
</template>

<script>
import cardsData from '@/components/cards/cards';
import echartsNew from '@/components/echartsNew/index';
import {
  getBalanceBasic,
  getBalanceTrend,
  getBalanceChannel,
  getBalanceType,
} from '@/api/statistic';
import { formatDate } from '@/utils/validate';
import echartsFrom from '@/components/echarts/index';
import timeOptions from '@/utils/timeOptions';
import { mapState } from "vuex";

export default {
  name: 'index',
  components: { cardsData, echartsNew, echartsFrom },
  data() {
    return {
      timeVal: [],
      style: { height: '400px' },
      infoList: {},
      infoList2: {},
      echartLeft: true,
      echartRight: false,
      loading: false,
      loading2: false,
      fromList: {
        title: '选择时间',
        custom: true,
        fromTxt: [
          { text: '全部', val: '' },
          { text: '今天', val: 'today' },
          { text: '本周', val: 'week' },
          { text: '本月', val: 'month' },
          { text: '本季度', val: 'quarter' },
          { text: '本年', val: 'year' },
        ],
      },
      formValidate: {
        time: '',
      },
      cardLists: [
        {
          col: 8,
          count: 0,
          name: '当前余额',
          className: 'md-rose',
        },
        {
          col: 8,
          count: 0,
          name: '累计充值余额',
          className: 'ios-speedometer-outline',
        },
        {
          col: 8,
          count: 0,
          name: '累计消耗余额',
          className: 'ios-speedometer-outline',
        },
      ],
      optionData: {},
      spinShow: false,
      options: timeOptions,
      columns: [
        {
          title: '序号',
          type: 'index',
          width: 60,
          align: 'center',
        },
        {
          title: '来源',
          key: 'name',
          minWidth: 80,
          align: 'center',
        },
        {
          title: '金额',
          width: 180,
          key: 'value',
          align: 'center',
        },
        {
          title: '占比率',
          slot: 'percent',
          minWidth: 100,
          align: 'center',
        },
      ],
      tabList: [],
      tabList2: [],
    };
  },
  computed:{
     ...mapState("admin/layout", ["isMobile"]),
    labelWidth() {
      return this.isMobile ? undefined : 96;
    },
    labelPosition() {
      return this.isMobile ? "top" : "right";
    },
  },
  created() {
    const end = new Date();
    const start = new Date();
    start.setTime(
      start.setTime(
        new Date(
          new Date().getFullYear(),
          new Date().getMonth(),
          new Date().getDate() - 29
        )
      )
    );
    this.timeVal = [start, end];
    this.formValidate.time =
      formatDate(start, 'yyyy/MM/dd') + '-' + formatDate(end, 'yyyy/MM/dd');
    this.onInit();
  },
  methods: {
    onInit() {
      this.getBalanceBasic();
      this.getBalanceTrend();
      this.getBalanceChannel();
      this.getBalanceType();
    },
    onSelectDate(e) {
      this.formValidate.time = e;
      this.onInit();
    },
    getBalanceBasic() {
      getBalanceBasic(this.formValidate).then((res) => {
        let arr = ['now_balance', 'add_balance', 'sub_balance'];
        this.cardLists.map((i, index) => {
          i.count = res.data[arr[index]];
        });
      });
    },
    getBalanceChannel() {
      this.loading = true;
      getBalanceChannel(this.formValidate).then((res) => {
        this.infoList = res.data;
        this.tabList = res.data.list;
        this.loading = false;
      });
    },
    getBalanceType() {
      this.loading2 = true;
      getBalanceType(this.formValidate).then((res) => {
        this.infoList2 = res.data;
        this.tabList2 = res.data.list;
        this.loading2 = false;
      });
    },
    // 具体日期
    onchangeTime(e) {
      this.timeVal = e;
      this.formValidate.time = this.timeVal[0] ? this.timeVal.join('-') : '';
      this.name = this.formValidate.time;
      this.getBalanceBasic();
      if (this.formValidate.time) {
        this.getBalanceTrend(); 
      }
    },
    // 统计图
    getBalanceTrend() {
      this.spinShow = true;
      getBalanceTrend(this.formValidate)
        .then(async (res) => {
          let legend = res.data.series.map((item) => {
            return item.name;
          });
          let xAxis = res.data.xAxis;
          let col = ['#5B8FF9', '#5AD8A6', '#FFAB2B', '#5D7092'];
          let series = [];
          res.data.series.map((item, index) => {
            series.push({
              name: item.name,
              type: 'line',
              data: item.data,
              itemStyle: {
                normal: {
                  color: col[index],
                },
              },
              smooth: 0,
            });
          });
          this.optionData = {
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'cross',
                label: {
                  backgroundColor: '#6a7985',
                },
              },
            },
            legend: {
              x: 'center',
              data: legend,
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
              containLabel: true,
            },
            toolbox: {
              feature: {
                saveAsImage: {},
              },
              right: '5%'
            },
            xAxis: {
              type: 'category',
              boundaryGap: true,
              axisLabel: {
                interval: 0,
                rotate: 40,
                textStyle: {
                  color: '#000000',
                },
              },
              data: xAxis,
            },
            yAxis: {
              type: 'value',
              axisLine: {
                show: false,
              },
              axisTick: {
                show: false,
              },
              axisLabel: {
                textStyle: {
                  color: '#7F8B9C',
                },
              },
              splitLine: {
                show: true,
                lineStyle: {
                  color: '#F5F7F9',
                },
              },
            },
            series: series,
          };
          this.spinShow = false;
        })
        .catch((res) => {
          this.$Message.error(res.msg);
          this.spinShow = false;
        });
    },
  },
};
</script>

<style scoped>
.cl {
  margin-right: 20px;
}
.code-row-bg {
  display: flex;
  flex-wrap: nowrap;
}
.code-row-bg .ivu-mt {
  min-width: 50%;
  margin: 0 5px;
}
.ech-box {
  margin-top: 10px;
}
.change-style {
  border: 1px solid #ccc;
  border-radius: 15px;
  padding: 0px 10px;
  cursor: pointer;
}
.percent-box {
  display: flex;
  align-items: center;
}
.line {
  flex: 1;
  position: relative;
}
.bg {
  position: absolute;
  width: 100%;
  height: 8px;
  border-radius: 8px;
  background-color: #f2f2f2;
}
.percent {
  position: absolute;
  border-radius: 5px;
  height: 8px;
  background-color: cornflowerblue;
  z-index: 9999;
}
.num {
  white-space: nowrap;
  margin: 0 0 0 10px;
  width: 50px;
}
</style>
