<template>
  <div class="goods_detail">
    <div class="goods_detail_wrapper" :class="product?'on':''" style="height: 640px">
      <HappyScroll size="5" resize hide-horizontal>
        <div style="width: 375px">
          <div class="title-box">商品详情</div>
          <div class="swiper-box">
            <Carousel autoplay v-model="value2" arrow="never">
              <CarouselItem
                v-for="(item, index) in goodsInfo.slider_image"
                :key="index"
              >
                <div class="demo-carousel"><img :src="item" alt="" /></div>
              </CarouselItem>
            </Carousel>
          </div>
          <div class="goods_info">
            <div class="number-wrapper">
              <div class="price"><span>¥</span>{{ goodsInfo.price }}</div>
              <div class="old-price" v-if="goodsInfo.vip_price > 0">
                ¥{{ goodsInfo.vip_price }}
                <img src="@/assets/images/goods_vip.png" alt="" width="28" />
              </div>
            </div>
            <div class="name">{{ goodsInfo.store_name }}</div>
            <div class="msg">
              <div class="item">划线价:￥{{ goodsInfo.ot_price }}</div>
              <div class="item">销量:{{ goodsInfo.sales }}</div>
              <div class="item">库存:{{ goodsInfo.stock }}</div>
            </div>
          </div>
          <div class="con-box">
            <div class="title-box">商品介绍</div>
            <div class="content" v-html="goodsInfo.description"></div>
          </div>
        </div>
      </HappyScroll>
    </div>
  </div>
</template>

<script>
import { HappyScroll } from "vue-happy-scroll";
import { productInfo } from "@/api/kefu";
import { productInfoApi } from "@/api/product";
export default {
  name: "goods_detail",
  props: {
    goodsId: {
      type: String | Number,
      default: "",
    },
    product: {
      type: String | Number,
      default: "",
    }
  },
  components: {
    HappyScroll,
  },
  data() {
    return {
      value2: 0,
      goodsInfo: {},
    };
  },
  mounted() {
    if(this.product){
      this.getInfoApi();
    }else {
      this.getInfo();
    }
  },
  methods: {
    getInfo() {
      productInfo(this.goodsId).then((res) => {
        this.goodsInfo = res.data;
      });
    },
    getInfoApi() {
      productInfoApi(this.goodsId)
         .then(async (res) => {
           this.goodsInfo = res.data.productInfo;
         })
         .catch((res) => {
           this.$Message.error(res.msg);
         });
    },
  },
};
</script>

<style lang="stylus" scoped>
.goods_detail {
  .goods_detail_wrapper {
    z-index: 200;
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    width: 375px;
    background: #F0F2F5;
    &.on{
      position fixed;
    }
  }

  .title-box {
    height: 46px;
    line-height: 46px;
    background: #fff;
    text-align: center;
    color: #333;
    font-size: 16px;
  }

  .swiper-box {
    height: 375px;

    .demo-carousel {
      width: 375px;
      height: 375px;

      img {
        width: 100%;
        height: 100%;
        display: block;
      }
    }
  }

  .goods_info {
    padding: 15px;
    background: #fff;

    .number-wrapper {
      display: flex;
      align-items: center;

      .price {
        color: #FF3838;
        font-size: 25px;

        span {
          font-size: 15px;
        }
      }

      .old-price {
        font-size: 15px;
        margin-left: 10px;
        color: #333333;
      }
    }

    .name {
      font-size: 16px;
      color: #333;
    }

    .msg {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 10px;

      .item {
        color: #999999;
        font-size: 14px;
      }
    }
  }

  .con-box {
    margin-top: 10px;
    padding-bottom: 20px;
  }
}
</style>
