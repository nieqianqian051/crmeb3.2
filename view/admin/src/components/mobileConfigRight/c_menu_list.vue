<template>
  <div class="hot_imgs">
    <div class="title" v-if="configData.title">
      {{ configData.title }}
    </div>
    <div class="list-box">
      <draggable
        class="dragArea list-group"
        :list="configData.list"
        group="peoples"
        handle=".move-icon"
      >
        <div class="item" v-for="(item, index) in configData.list" :key="index">
          <div
            class="delect-btn"
            @click.stop="bindDelete(item, index)"
            v-if="!configData.isCube"
          >
            <span class="iconfont-diy icondel_1"></span>
          </div>
          <div class="move-icon">
            <span class="iconfont-diy iconxingzhuangjiehe"></span>
          </div>
          <div class="img-box" @click="modalPicTap('单选', index)">
            <img :src="item.img" alt="" v-if="item.img" />
            <div class="upload-box" v-else>
              <Icon type="ios-add" size="50" />
            </div>
          </div>
          <div class="info">
            <div class="info-item" v-for="(infos, key) in item.info" :key="key">
              <span class="span">{{ infos.title }}</span>
              <div class="input-box">
                <Input
                  :icon="key == item.info.length - 1 ? 'ios-link' : ''"
                  v-model="infos.value"
                  :placeholder="infos.tips"
                  :maxlength="infos.max"
                  v-if="configData.isCube"
                  @on-blur="onBlur"
                  @on-click="getLink(index, key, item.info)"
                  search
                />
                <Input
                  :icon="key == item.info.length - 1 ? 'ios-link' : ''"
                  v-model="infos.value"
                  :placeholder="infos.tips"
                  :maxlength="infos.max"
                  show-word-limit
                  @on-click="getLink(index, key, item.info)"
                  search
                  v-else
                />
              </div>
            </div>
            <div class="info-item" v-if="configData.type">
              <span class="span">状态</span>
              <Switch v-model="item.show" />
            </div>
            <div class="flex-between-center" v-if="item.showFit">
              <span class="fs-12">缩放模式</span>
              <div class="flex-y-center">
                <RadioGroup v-model="item.objectFit" @on-change="fitChange">
                  <Radio label="cover">填充</Radio>
                  <Radio label="contain">缩放</Radio>
                  <Radio label="fill">拉伸</Radio>
                </RadioGroup>
              </div>
            </div>
          </div>
        </div>
      </draggable>
    </div>
    <template v-if="configData.list">
      <div class="add-btn" v-if="configData.list.length < configData.maxList">
        <Button class="btn" type="primary" ghost @click="addBox">
          <span class="iconfont iconjiahao"></span>{{ configData.bnt }}
        </Button>
      </div>
    </template>
    <linkaddress ref="linkaddres" @linkUrl="linkUrl"></linkaddress>
  </div>
</template>

<script>
import vuedraggable from "vuedraggable";
import uploadPictures from "@/components/uploadPictures";
import linkaddress from "@/components/linkaddress";
export default {
  name: "c_menu_list",
  props: {
    configObj: {
      type: Object,
    },
    configNme: {
      type: String,
    },
    index: {
      type: null,
    },
  },
  components: {
    draggable: vuedraggable,
    linkaddress,
    uploadPictures,
  },
  data() {
    return {
      defaults: {},
      configData: {},
      menus: [],
      list: [
        {
          title: "aa",
          val: "",
        },
      ],
      activeIndex: 0,
      indexLast: 0,
      lastObj: {},
    };
  },
  mounted() {
    this.$nextTick(() => {
      this.defaults = this.configObj;
      this.configData = this.configObj[this.configNme];
    });
  },
  watch: {
    configObj: {
      handler(nVal, oVal) {
        this.defaults = nVal;
        this.configData = nVal[this.configNme];
      },
      deep: true,
    },
  },
  methods: {
    linkUrl(e) {
      this.configData.list[this.activeIndex].info[this.indexLast].value = e;
      if (this.defaults.name == "pictureCube") {
        this.defaults.picStyle.picList[this.defaults.picStyle.tabVal].link = e;
      }
    },
    getLink(index, key, item) {
      this.indexLast = item.length - 1;
      if (key != item.length - 1) {
        return;
      }
      this.activeIndex = index;
      this.$refs.linkaddres.modals = true;
    },
    addBox() {
      if (this.configData.list.length == 0) {
        this.lastObj.img = "";
        this.lastObj.info[0].value = "";
        this.lastObj.info[1].value = "";
        this.configData.list.push(this.lastObj);
      } else {
        let obj = JSON.parse(
          JSON.stringify(this.configData.list[this.configData.list.length - 1])
        );
        obj.img = "";
        obj.info[0].value = "";
        obj.info[1].value = "";
        this.configData.list.push(obj);
      }
    },
    // 点击图文封面
    modalPicTap(title, index) {
      this.activeIndex = index;
      this.$imgModal((e) => {
        let imgUrl = e[0].att_dir;
        if (imgUrl.includes("mp4")) {
          this.$Message.error("请选择正确的图片文件");
        } else {
          this.$nextTick(() => {
            this.configData.list[index].img = imgUrl;
            let data = this.defaults.menuConfig;
            if (data && data.isCube) {
              this.defaults.picStyle.picList.splice(
                this.defaults.picStyle.tabVal,
                1,
                {
                  image: imgUrl,
                  link: data.list[0].info[0].value,
                  objectFit: this.configData.list[index].objectFit
                }
              );
            }
            this.modalPic = false;
          });
        }
      });
    },
    onBlur() {
      let data = this.defaults.menuConfig;
      this.defaults.picStyle.picList[this.defaults.picStyle.tabVal].link =
        data.list[0].info[0].value;
    },
    // 删除
    bindDelete(item, index) {
      if (this.configData.list.length == 1) {
        this.lastObj = this.configData.list[0];
      }
      this.configData.list.splice(index, 1);
    },
    fitChange(val){
      this.defaults.picStyle.picList[this.defaults.picStyle.tabVal].objectFit = val;
    }
  },
};
</script>

<style scoped lang="stylus">
/deep/.ivu-input-icon {
  color: #BBBBBB;
}

/deep/.ivu-input-word-count {
  color: #BBBBBB;
}

.hot_imgs {
  margin: 0 15px 20px 15px;

  .title {
    padding-bottom: 21px;
    color: #999;
    font-size: 12px;
  }

  .list-box {
    .item {
      position: relative;
      display: flex;
      background: #F9F9F9;
      align-items: center;
      padding: 16px 20px 16px 0;
      margin-bottom: 16px;
      border-radius: 3px;

      .delect-btn {
        position: absolute;
        right: -13px;
        top: -16px;

        .iconfont-diy {
          font-size: 25px;
          color: #ccc;
        }
      }

      .move-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 30px;
        cursor: move;
      }

      .img-box {
        position: relative;
        width: 64px;
        height: 64px;

        img {
          width: 100%;
          height: 100%;
          border-radius: 3px;
        }
      }

      .info {
        flex: 1;
        margin-left: 22px;

        .info-item {
          display: flex;
          align-items: center;
          margin-bottom: 10px;

          &:nth-last-child(1) {
            margin-bottom: 0;
          }

          .span {
            width: 40px;
            font-size: 12px;
            color: #999;
          }

          .input-box {
            flex: 1;
          }
        }
      }
    }
  }

  .add-btn {
    margin-top: 10px;

    .btn {
      width: 100%;
      height: 36px;
      border-color: #EEEEEE;
      color: #666666;

      .iconfont {
        font-size: 11px;
        margin-right: 5px;
      }
    }
  }
}

.upload-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #EEEEEE;
  color: #ccc
}

.iconfont-diy {
  color: #DDDDDD;
  font-size: 16px;
}
</style>
