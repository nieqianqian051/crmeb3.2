<template>
<!-- 装修-主题风格 -->
    <div>
      <div class="i-layout-page-header">
        <!-- 顶部标题 -->
        <PageHeader
            class="product_tabs"
            :title="$route.meta.title"
            hidden-breadcrumb
            :style="'padding-right:'+(menuCollapse?105:20)+'px'"
        >
          <div slot="title">
            <div style="float: left;">
              <span v-text="$route.meta.title" class="mr20"></span>
            </div>
          </div>
        </PageHeader>
      </div>
        <Card :bordered="false" dis-hover class="ivu-mt" :style="'min-height:'+clientHeight+'px'">
            <Form :label-width="labelWidth">
                <FormItem label="选择配色方案：">
                    <RadioGroup v-model="current" @on-change="changeColor">
                        <Radio :label="1" border class="box">天空蓝<i class="iconfont iconxuanzhong6"></i></Radio>
                        <Radio :label="2" border class="box green">生鲜绿<i class="iconfont iconxuanzhong6"></i></Radio>
                        <Radio :label="3" border class="box red">热情红<i class="iconfont iconxuanzhong6"></i></Radio>
                        <Radio :label="4" border class="box pink">魅力粉<i class="iconfont iconxuanzhong6"></i></Radio>
                        <Radio :label="5" border class="box orange">活力橙<i class="iconfont iconxuanzhong6"></i></Radio>
						            <Radio :label="6" border class="box gold">高端金<i class="iconfont iconxuanzhong6"></i></Radio>
                    </RadioGroup>
                </FormItem>
                <FormItem label="当前风格示例：">
                    <div class="acea-row row-top">
                        <div class="pictrue">
                            <img :src="imgColor.image">
                        </div>
                    </div>
                </FormItem>
            </Form>
        </Card>
        <Card :bordered="false" dis-hover class="fixed-card" :style="{left: `${!menuCollapse?'200px':isMobile?'0':'80px'}`}">
            <div class="acea-row row-center-wrapper">
                <Button class="bnt" type="primary" @click="submit" :loading="loadingExist">保存</Button>
            </div>
        </Card>
    </div>
</template>

<script>
    import { mapState } from "vuex";
    import { colorChange, getColorChange } from "@/api/diy";
    export default {
        name: "themeStyle",
        data() {
            return {
                grid: {
                    xl: 7,
                    lg: 7,
                    md: 12,
                    sm: 24,
                    xs: 24,
                },
                imgColor:{},
                picList:[
                  {image:require('@/assets/images/blue.jpg')},
                  {image:require('@/assets/images/green.jpg')},
                  {image:require('@/assets/images/red.jpg')},
                  {image:require('@/assets/images/pink.jpg')},
                  {image:require('@/assets/images/orange.jpg')},
                  {image:require('@/assets/images/gold.jpg')}
                ],
                current:'',
                clientHeight:0,
                loadingExist:false
            };
        },
        computed: {
            ...mapState("admin/layout", ["isMobile","menuCollapse"]),
            labelWidth() {
                return this.isMobile ? undefined : 100;
            },
            labelPosition() {
                return this.isMobile ? "top" : "right";
            }
        },
        created() {
            this.imgColor = this.picList[0];
            this.getInfo();
        },
        mounted: function() {
            this.$nextTick(()=>{
                this.clientHeight = `${document.documentElement.clientHeight}`-250;//获取浏览器可视区域高度
                let that = this;
                window.onresize = function(){
                    that.clientHeight =  `${document.documentElement.clientHeight}`-250;
                }
            })
        },
        methods: {
            getInfo(){
                getColorChange('color_change').then(res=>{
                    this.current = res.data.status?res.data.status:3
                    this.changeColor(this.current);
                }).catch((err)=>{
                    this.$Message.error(err.msg);
                })
            },
            submit(){
                this.loadingExist = true
                colorChange(this.current,'color_change').then(res=>{
                    this.loadingExist = false
                    this.$Message.success(res.msg);
                }).catch(()=>{
                    this.loadingExist = false
                })
            },
            changeColor(e){
                switch(e){
                    case 1:
                        this.imgColor = this.picList[0];
                        break;
                    case 2:
                        this.imgColor = this.picList[1];
                        break;
                    case 3:
                        this.imgColor = this.picList[2];
                        break;
                    case 4:
                        this.imgColor = this.picList[3];
                        break;
                    case 5:
                        this.imgColor = this.picList[4];
                        break;
					case 6:
					    this.imgColor = this.picList[5];
					    break;
                    default:
                        break
                }
            }
        },
    };
</script>

<style scoped lang="stylus">
	.box{
		height 40px
		width 100px
		line-height 40px
		text-align center;
	}
    .bnt{
        // width 10px!important;
    }
    .pictrue{
        width: 800px;
        height: 100%;
        margin: 10px 24px 0 0;
        img{
			width 100%;
			height 100%;
        }
    }
    .footer{
        width 100%
        height 70px
        box-shadow: 0px -2px 4px rgba(0, 0, 0, 0.03);
        background-color #fff
        position fixed;
        bottom 0;
        left:0
        z-index 9
    }
	.fixed-card {
      position: fixed;
      right: 0;
      bottom: 0;
      left: 200px;
      z-index: 99;
      box-shadow: 0 -1px 2px rgb(240, 240, 240);
    }
    /deep/.i-layout-content-main{
        margin-bottom 0!important
    }

    /deep/.ivu-radio-inner{
        background-color: #1db0fc;
        border:0;
        border-radius: 3px;
        width: 18px;
        height: 18px;
    }
    /deep/.ivu-radio-wrapper-checked .iconfont{
        display: inline-block;
    }
    /deep/.ivu-radio-focus{
        box-shadow: unset;
        z-index: unset;
    }
    /deep/.ivu-radio-wrapper{
       margin-right: 18px;
    }
    .green /deep/.ivu-radio-inner{
        background-color: #42CA4D;
    }
    .red /deep/.ivu-radio-inner{
        background-color: #E93323;
    }
    .pink/deep/.ivu-radio-inner{
        background-color: #FF448F;
    }
    .orange/deep/.ivu-radio-inner{
        background-color: #FE5C2D;
    }
    .gold/deep/.ivu-radio-inner{
      background-color: #E0A558;
    }
    /deep/.ivu-radio-border{
        position: relative;
    }
    .iconfont{
        position: absolute;
        top:0px;
        left: 21px;
        font-size: 12px;
        display: none;
        color: #fff;
    }
    /deep/.ivu-radio-inner:after{
        background-color: unset;
        transform:unset;
    }
</style>
