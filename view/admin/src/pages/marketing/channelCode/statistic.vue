<template>
  <div>
    <div class="i-layout-page-header">
      <PageHeader class="product_tabs" hidden-breadcrumb>
        <div slot="title">
          <router-link :to="{ path: `/admin/marketing/channel_code` }">
            <div class="font-sm after-line">
              <span class="iconfont iconfanhui"></span>
              <span class="pl10">返回</span>
            </div>
          </router-link>
          <span v-text="$route.meta.title" class="ml16"></span>
        </div>
      </PageHeader>
    </div>
    <cards-data
      :cardLists="cardLists"
      v-if="cardLists.length >= 0"
    ></cards-data>
    <Card :bordered="false" dis-hover class="ivu-mt">
      <div class="table-head">
        <h3>关注趋势</h3>
        <DatePicker
          :editable="false"
          :clearable="false"
          @on-change="onchangeTime"
          :value="timeVal"
          format="yyyy/MM/dd"
          type="daterange"
          placement="bottom-start"
          placeholder="请选择时间"
          style="width: 200px"
          :options="options"
          class="mr20"
        ></DatePicker>
      </div>
      <echarts-new
        :option-data="optionData"
        :styles="style"
        height="100%"
        width="100%"
        v-if="optionData"
      ></echarts-new>
    </Card>
    <Spin size="large" fix v-if="spinShow"></Spin>
  </div>
</template>

<script>
import echartsFrom from '@/components/echarts/index';
import cardsData from '@/components/cards/cards';
import echartsNew from '@/components/echartsNew/index';
import { wechatQrcodeStatistic } from '@/api/marketing';
import { formatDate } from '@/utils/validate';
import Setting from '@/setting';

export default {
  name: 'marketing_channelStatistic',
  components: { cardsData, echartsNew, echartsFrom },
  data() {
    return {
      roterPre: Setting.roterPre,
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
          col: 6,
          count: 0,
          name: '昨日新增关注',
          className: 'md-rose',
        },
        {
          col: 6,
          count: 0,
          name: '昨日参与',
          className: 'ios-speedometer-outline',
        },
        {
          col: 6,
          count: 0,
          name: '总关注',
          className: 'ios-speedometer-outline',
        },
        {
          col: 6,
          count: 0,
          name: '参与总人数',
          className: 'md-rose',
        },
      ],
      optionData: {},
      spinShow: false,
      options: this.$timeOptions,
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
    };
  },
  created() {
    this.id = this.$route.query.id;
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
    this.wechatQrcodeStatistic();
  },
  methods: {
    wechatQrcodeStatistic() {
      wechatQrcodeStatistic(this.id, this.formValidate)
        .then((res) => {
          let arr = ['y_follow', 'y_scan', 'all_follow', 'all_scan'];
          this.cardLists.map((i, index) => {
            i.count = res.data[arr[index]];
          });
          this.getTrend(res.data.trend.series, res.data.trend.xAxis);
        });
    },
    timeG(dd) {
      var d = new Date(dd);
      var datetime =
        d.getFullYear() +
        '-' +
        (d.getMonth() + 1) +
        '-' +
        d.getDate() +
        ' ' +
        d.getHours() +
        ':' +
        d.getMinutes() +
        ':' +
        d.getSeconds();
      return datetime;
    },
    // 具体日期
    onchangeTime(e) {
      this.timeVal = e;
      this.formValidate.time = this.timeVal[0] ? this.timeVal.join('-') : "";
      this.name = this.formValidate.time;
      this.wechatQrcodeStatistic();
    },
    // 统计图
    getTrend(seriesData, xAxisData) {
      this.spinShow = true;
      let legend = seriesData.map((item) => {
        return item.name;
      });
      let xAxis = xAxisData;
      let col = ['#5B8FF9', '#5AD8A6', '#FFAB2B', '#5D7092'];
      let series = [];
      seriesData.map((item, index) => {
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
  width: 100%;
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
.table-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.return {
  margin-bottom: 6px;
}
</style>
