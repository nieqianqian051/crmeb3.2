<template>
  <div class="Modal">
    <Row class="colLeft">
      <Col :xl="6" :lg="6" :md="6" :sm="6" :xs="24" class="colLeft left">
      <div class="Nav">
        <div class="input">
          <Input search enter-button placeholder="请输入分类名称" v-model="uploadName.name" style="width: 90%"
            @on-search="changePage" />
        </div>
        <div class="trees-coadd">
          <div class="scollhide">
            <div class="trees">
              <Tree :data="treeData" :render="renderContent" :load-data="loadData" class="treeBox" ref="tree"></Tree>
              <div class="searchNo" v-if="searchClass && treeData.length <= 1">此分类暂无数据</div>
            </div>
          </div>
        </div>
      </div>
      </Col>
      <Col :xl="18" :lg="18" :md="18" :sm="18" :xs="24" class="colLeft right">
      <div class="conter">
        <div class="bnt acea-row row-middle">
          <Col span="24">
          <Button type="primary" :disabled="checkPicList.length === 0" @click="checkPics" class="mr10"
            v-if="isShow !== 0" style="width: 100px;">使用选中图片</Button>
          <!-- <Upload :show-upload-list="false" :action="fileUrl" class="mr10 mb10" :before-upload="beforeUpload"
            :data="uploadData" :headers="header" :multiple="true" :on-success="handleSuccess"
            style="margin-top: 1px; display: inline-block">
            <Button type="primary">上传图片</Button>
          </Upload> -->
          <Button type="primary" @click="openUpload" class="mr10">上传图片</Button>
          <!--<Button type="success" @click.stop="add" class="mr10">添加分类</Button>-->
          <Button type="error" class="mr10" :disabled="checkPicList.length === 0"
            @click.stop="editPicList('图片')">删除图片</Button>
          <i-select :value="pids" placeholder="图片移动至" style="width: 250px" class="treeSel">
            <i-option v-for="(item, index) of list" :value="item.value" :key="index" style="display: none">
              {{ item.title }}
            </i-option>
            <Tree :data="treeData2" :render="renderContentSel" ref="reference" :load-data="loadData" class="treeBox">
            </Tree>
          </i-select>
          </Col>
        </div>
        <div class="pictrueList acea-row">
          <Row :gutter="24" class="conter">
            <div v-show="isShowPic" class="imagesNo">
              <Icon type="ios-images" size="60" color="#dbdbdb" />
              <span class="imagesNo_sp">图片库为空</span>
            </div>
            <div class="acea-row mb10">
              <div class="pictrueList_pic mr10 mb10" v-for="(item, index) in pictrueList" :key="index"
                @mouseenter="enterMouse(item)" @mouseleave="enterMouse(item)">
                <p class="number" v-if="item.num > 0">
                  <Badge :count="item.num" type="error" :offset="[11, 12]">
                    <a href="#" class="demo-badge"></a>
                  </Badge>
                </p>
                <img :class="item.isSelect ? 'on' : ''" v-lazy="item.satt_dir"
                  @click.stop="changImage(item, index, pictrueList)" />
                <div style="
                      display: flex;
                      align-items: center;
                      justify-content: space-between;
                    " @mouseenter="enterLeave(item)" @mouseleave="enterLeave(item)">
                  <p v-if="!item.isEdit">
                    {{ item.editName }}
                  </p>
                  <Input size="small" style="width: 80%" type="text" v-model="item.real_name" v-else
                    @on-blur="bindTxt(item)" />
                  <span class="iconfont iconbianji1" @click="item.isEdit = !item.isEdit" v-if="item.isShowEdit"></span>
                </div>
                <div class="nameStyle" v-show="item.realName && item.real_name">
                  {{ item.real_name }}
                </div>
              </div>
            </div>
            <!--<Col class="mb20" v-bind="gridPic"-->
            <!--v-for="(item, index) in pictrueList" :key="index" >-->
            <!--<div class="pictrueList_pic">-->
            <!--<img :class="item.isSelect ? 'on': '' " v-lazy="item.satt_dir"-->
            <!--@click.stop="changImage(item, index, pictrueList)"/>-->
            <!--</div>-->
            <!--</Col>-->
          </Row>
        </div>
        <div class="footer acea-row row-right">
          <Page :total="total" show-elevator show-total @on-change="pageChange" :current="fileData.page"
            :page-size="fileData.limit" />
        </div>
      </div>
      </Col>
    </Row>
    <!-- 上传图片 -->
    <uploadImg v-model="uploadVisible" :category-list="treeData" :category-id="treeId" @uploadSuccess="uploadSuccess"></uploadImg>
  </div>
</template>

<script>
import {
  getCategoryListApi,
  createApi,
  fileListApi,
  categoryEditApi,
  moveApi,
  fileUpdateApi,
} from "@/api/uploadPictures";
import Setting from "@/setting";
// import { getCookies } from "@/libs/util";
import util from "@/libs/util";
import uploadImg from "@/components/uploadImg";
export default {
  name: "uploadPictures",
  // components: { editFrom },
  components: { uploadImg },
  props: {
    isChoice: {
      type: String,
      default: "",
    },
    gridBtn: {
      type: Object,
      default: null,
    },
    gridPic: {
      type: Object,
      default: null,
    },
    isShow: {
      type: Number,
      default: 1,
    }
  },
  data() {
    return {
      searchClass: false,
      spinShow: false,
      fileUrl: Setting.apiBaseURL + "/file/upload",
      modalPic: false,
      treeData: [],
      treeData2: [],
      pictrueList: [],
      uploadData: {}, // 上传参数
      checkPicList: [],
      uploadName: {
        name: "",
      },
      FromData: null,
      treeId: 0,
      isJudge: false,
      buttonProps: {
        type: "default",
        size: "small",
      },
      fileData: {
        pid: 0,
        page: 1,
        limit: 24,
      },
      total: 0,
      pids: 0,
      list: [],
      modalTitleSs: "",
      isShowPic: false,
      header: {},
      ids: [], // 选中附件的id集合
      uploadList:[],
      uploadVisible: false,
    };
  },
  mounted() {
    this.getToken();
    this.getList();
    this.getFileList();
  },
  methods: {
    enterMouse(item) {
      item.realName = !item.realName;
    },
    enterLeave(item) {
      item.isShowEdit = !item.isShowEdit;
    },
    // 上传头部token
    getToken() {
      this.header["Authori-zation"] = "Bearer " + util.cookies.get("token");
    },
    // 树状图
    renderContent(h, { root, node, data }) {
      let operate = [];
      if (data.pid == 0) {
        operate.push(
          h(
            "div",
            {
              class: ["ivu-dropdown-item"],
              on: {
                click: () => {
                  this.append(root, node, data);
                },
              },
            },
            "添加分类"
          )
        );
      }
      if (data.id !== "") {
        operate.push(
          h(
            "div",
            {
              class: ["ivu-dropdown-item"],
              on: {
                click: () => {
                  this.editPic(root, node, data);
                },
              },
            },
            "编辑分类"
          ),
          h(
            "div",
            {
              class: ["ivu-dropdown-item"],
              on: {
                click: () => {
                  this.remove(root, node, data, "分类");
                },
              },
            },
            "删除分类"
          )
        );
      }
      return h(
        "span",
        {
          class: ["ivu-span"],
          style: {
            display: "inline-block",
            width: "88%",
            height: "32px",
            lineHeight: "32px",
            position: "relative",
            color: "rgba(0,0,0,0.6)",
            cursor: "pointer",
          },
          on: {
            mouseenter: () => {
              this.onMouseOver(root, node, data);
            },
            mouseleave: () => {
              this.onMouseOver(root, node, data);
            },
          },
        },
        [
          h(
            "span",
            {
              on: {
                click: (e) => {
                  this.checkPicList =[]
                  this.appendBtn(root, node, data, e);
                },
              },
            },
            data.title
          ),
          h(
            "div",
            {
              style: {
                display: "inline-block",
                float: "right",
              },
            },
            [
              h("Icon", {
                props: {
                  type: "ios-more",
                },
                style: {
                  marginRight: "8px",
                  fontSize: "20px",
                  display: data.flag ? "inline" : "none",
                },
                on: {
                  click: () => {
                    this.onClick(root, node, data);
                  },
                },
              }),
              h(
                "div",
                {
                  class: ["right-menu ivu-poptip-inner"],
                  style: {
                    width: "80px",
                    position: "absolute",
                    zIndex: "9",
                    top: "0",
                    right: "0",
                    display: data.flag2 ? "block" : "none",
                  },
                },
                operate
              ),
            ]
          ),
        ]
      );
    },
    // renderContent (h, { root, node, data }) {
    //     let actionData = [];
    //     if (data.id !== '' && data.pid == 0) {
    //         actionData.push(h('Button', {
    //             props: Object.assign({}, this.buttonProps, {
    //                 icon: 'ios-add'
    //             }),
    //             style: {
    //                 marginRight: '8px',
    //                 display: data.flag ? 'inline' : 'none'
    //             },
    //             on: {
    //                 click: () => { this.append(root, node, data) }
    //
    //             }
    //         }));
    //     }
    //     if (data.id !== '') {
    //         actionData.push(h('Button', {
    //             props: Object.assign({}, this.buttonProps, {
    //                 icon: 'md-create'
    //             }),
    //             style: {
    //                 marginRight: '8px',
    //                 display: data.flag ? 'inline' : 'none'
    //             },
    //             on: {
    //                 click: () => { this.editPic(root, node, data) }
    //             }
    //         }));
    //         actionData.push(h('Button', {
    //             props: Object.assign({}, this.buttonProps, {
    //                 icon: 'ios-remove'
    //             }),
    //             style: {
    //                 display: data.flag ? 'inline' : 'none'
    //             },
    //             on: {
    //                 click: () => { this.remove(root, node, data, '分类') }
    //             }
    //         }));
    //     }
    //     return h('div', {
    //         style: {
    //             display: 'inline-block',
    //             width: '90%'
    //         },
    //         on: {
    //             mouseenter: () => { this.onMouseOver(root, node, data) },
    //             mouseleave: () => { this.onMouseOver(root, node, data) }
    //         }
    //     }, [
    //         h('span', [
    //             h('span', {
    //                 style: {
    //                     cursor: 'pointer'
    //                 },
    //                 class: ['ivu-tree-title'],
    //                 on: {
    //                     click: (e) => { this.appendBtn(root, node, data, e) }
    //                 }
    //             }, data.title)
    //         ]),
    //         h('span', {
    //             style: {
    //                 display: 'inline-block',
    //                 float: 'right'
    //             }
    //         }, actionData)
    //     ]);
    // },
    renderContentSel(h, { root, node, data }) {
      return h(
        "div",
        {
          style: {
            display: "inline-block",
            width: "90%",
          },
        },
        [
          h("span", [
            h(
              "span",
              {
                style: {
                  cursor: "pointer",
                },
                class: ["ivu-tree-title"],
                on: {
                  click: (e) => {
                    this.handleCheckChange(root, node, data, e);
                  },
                },
              },
              data.title
            ),
          ]),
        ]
      );
    },
    // 下拉树
    handleCheckChange(root, node, data, e) {
      this.list = [];
      // this.pids = 0;
      let value = data.id;
      let title = data.title;
      this.list.push({
        value,
        title,
      });
      if (this.ids.length) {
        this.pids = value;
        this.getMove();
      } else {
        this.$Message.warning("请先选择图片");
      }
      let selected = this.$refs.reference.$el.querySelectorAll(
        ".ivu-tree-title-selected"
      );
      for (let i = 0; i < selected.length; i++) {
        selected[i].className = "ivu-tree-title";
      }
      e.path[0].className = "ivu-tree-title  ivu-tree-title-selected"; // 当前点击的元素
    },
    // 移动分类
    getMove() {
      let data = {
        pid: this.pids,
        images: this.ids.toString(),
      };
      moveApi(data)
        .then(async (res) => {
          this.$Message.success(res.msg);
          this.getFileList();
          this.pids = 0;
          this.checkPicList = [];
          this.ids = [];
        })
        .catch((res) => {
          this.$Message.error(res.msg);
        });
    },
    // 删除图片
    editPicList(tit) {
      this.tits = tit;
      let ids = {
        ids: this.ids.toString(),
      };
      let delfromData = {
        title: "删除选中图片",
        url: `file/file/delete`,
        method: "POST",
        ids: ids,
      };
      this.$modalSure(delfromData)
        .then((res) => {
          this.$Message.success(res.msg);
          this.getFileList();
          this.checkPicList = [];
        })
        .catch((res) => {
          this.$Message.error(res.msg);
        });
    },
    // 鼠标移入 移出
    onMouseOver(root, node, data) {
      // console.log('sss333',data);
      event.preventDefault();
      data.flag = !data.flag;
      if (data.flag2) {
        data.flag2 = false;
      }
    },
    onClick(root, node, data) {
      data.flag2 = !data.flag2;
    },
    // 点击树
    appendBtn(root, node, data, e) {
      this.treeId = data.id;
      this.fileData.page = 1;
      this.getFileList();
      let selected = this.$refs.tree.$el.querySelectorAll(
        ".ivu-tree-title-selected"
      );
      for (let i = 0; i < selected.length; i++) {
        selected[i].className = "ivu-tree-title";
      }
      e.path[0].className = "ivu-tree-title  ivu-tree-title-selected"; // 当前点击的元素
    },
    // 点击添加
    append(root, node, data) {
      this.treeId = data.id;
      this.getFrom();
    },
    // 删除分类
    remove(root, node, data, tit) {
      this.tits = tit;
      let delfromData = {
        title: "删除 [ " + data.title + " ] " + "分类",
        url: `file/category/${data.id}`,
        method: "DELETE",
        ids: "",
      };
      this.$modalSure(delfromData)
        .then((res) => {
          this.$Message.success(res.msg);
          this.getList();
          this.checkPicList = [];
        })
        .catch((res) => {
          this.$Message.error(res.msg);
        });
    },
    // 确认删除树
    // submitModel () {
    //     if (this.tits === '图片') {
    //         this.getFileList();
    //         this.checkPicList = [];
    //     } else {
    //         this.getList();
    //         this.checkPicList = [];
    //     }
    // },
    // 编辑树表单
    editPic(root, node, data) {
      this.$modalForm(categoryEditApi(data.id)).then(() => this.getList());
    },
    // 搜索分类
    changePage() {
      this.getList("search");
    },
    // 分类列表树
    getList(type) {
      let data = {
        title: "全部图片",
        id: "",
        pid: 0,
      };
      getCategoryListApi(this.uploadName)
        .then(async (res) => {
          this.treeData = res.data.list;
          this.treeData.unshift(data);
          if (type !== "search") {
            this.treeData2 = [...this.treeData];
          } else {
            this.searchClass = true
          }
          this.addFlag(this.treeData);
        })
        .catch((res) => {
          this.$Message.error(res.msg);
        });
    },
    loadData(item, callback) {
      getCategoryListApi({
        pid: item.id,
      })
        .then(async (res) => {
          const data = res.data.list;
          callback(data);
        })
        .catch((res) => { });
    },
    addFlag(treedata) {
      treedata.map((item) => {
        this.$set(item, "flag", false);
        this.$set(item, "flag2", false);
        item.children && this.addFlag(item.children);
      });
    },
    // 新建分类
    add() {
      this.treeId = 0;
      this.getFrom();
    },
    // 文件列表
    getFileList() {
      this.fileData.pid = this.treeId;
      fileListApi(this.fileData)
        .then(async (res) => {
          res.data.list.forEach((el) => {
            el.isSelect = false;
            el.isEdit = false;
            el.isShowEdit = false;
            el.realName = false;
            el.num = 0;
            this.editName(el);
          });
          this.pictrueList = res.data.list;

          if (this.pictrueList.length) {
            this.isShowPic = false;
          } else {
            this.isShowPic = true;
          }
          this.total = res.data.count;
        })
        .catch((res) => {
          this.$Message.error(res.msg);
        });
    },
    pageChange(index) {
      this.fileData.page = index;
      this.getFileList();
      this.checkPicList = [];
    },
    // 新建分类表单
    getFrom() {
      this.$modalForm(createApi({ id: this.treeId })).then((res) => {
        console.log(res);
        this.getList();
      });
    },
    // 图片上传之前
    beforeUpload(res) {
      if (this.uploadList.length > 4) {
        // this.$Message.warning("一次最多只能上传5张图片");
        return false;
      }
      //控制文件上传格式
      let imgTypeArr = ["image/png", "image/jpg", "image/jpeg","image/gif"];     //限制图片上传类型
      let imgType = imgTypeArr.indexOf(res.type) !== -1
      if (!imgType) {
        // 图片上传异常
        this.$Message.warning({
          content:  '文件  ' + res.name + '  格式不正确, 请选择格式正确的图片',
          duration: 5
        });
        return false
      }
      // 控制文件上传大小
      console.log(res.size,'文件大小');
      // 限制图片上传大小
      let imgSize = localStorage.getItem('file_size_max');
      let Maxsize = res.size  < imgSize;
      let fileMax = imgSize/ 1024 / 1024;
      if (!Maxsize) {
        // 上传的图片大于自己指定的大小
        this.$Message.warning({
          content: '文件体积过大,图片大小不能超过' + fileMax + 'M',
          duration: 5
        });
        return false
      }
      this.uploadList.push(res);
      this.uploadData = {
        pid: this.treeId,
      };
      let promise = new Promise((resolve) => {
        this.$nextTick(function () {
          resolve(true);
        });
      });
      return promise;
    },
    // 图片上传成功
    handleSuccess(res, file, fileList) {
      if (res.status === 200) {
        this.uploadList = [];
        this.fileData.page = 1
        this.$Message.success(res.msg);
        this.getFileList();
      } else {
        this.$Message.error(res.msg);
      }
    },
    // 关闭
    cancel() {
      this.$emit("changeCancel");
    },
    // 选中图片
    changImage(item, index, row) {
      let activeIndex = 0;
      if (!item.isSelect) {
        item.isSelect = true;
        this.checkPicList.push(item);
      } else {
        item.isSelect = false;
        this.checkPicList.map((el, index) => {
          if (el.att_id == item.att_id) {
            activeIndex = index;
          }
        });
        this.checkPicList.splice(activeIndex, 1);
      }

      this.ids = [];
      this.checkPicList.map((item, i) => {
        this.ids.push(item.att_id);
      });
      this.pictrueList.map((el, i) => {
        if (el.isSelect) {
          this.checkPicList.filter((el2, j) => {
            if (el.att_id == el2.att_id) {
              el.num = j + 1;
            }
          });
        } else {
          el.num = 0;
        }
      });
    },
    // 点击使用选中图片
    checkPics() {
      if (!this.$route) {
          this.$emit("getPicD", this.checkPicList);
        } else {
          let maxLength = this.$route.query.maxLength;
          if (
            maxLength != undefined &&
            this.checkPicList.length > Number(maxLength)
          )
            return this.$Message.warning("最多只能选" + maxLength + "张图片");
          this.$emit("getPicD", this.checkPicList);
        }
      // if (this.isChoice === "单选") {
      //   if (this.checkPicList.length > 1)
      //     return this.$Message.warning("最多只能选一张图片");
      //   this.$emit("getPic", this.checkPicList[0]);
      // } else {
      //   let maxLength = this.$route.query.maxLength;
      //   if (
      //     maxLength != undefined &&
      //     this.checkPicList.length > Number(maxLength)
      //   )
      //     return this.$Message.warning("最多只能选" + maxLength + "张图片");
      //   this.$emit("getPicD", this.checkPicList);
      //   console.log(this.checkPicList);
      // }
    },
    editName(item) {
      let it = item.real_name.split(".");
      let it1 = it[1] == undefined ? [] : it[1];
      let len = it[0].length + it1.length;
      item.editName = item.real_name;
      // item.editName =
      //         len < 10
      //                 ? item.real_name
      //                 : item.real_name.substr(0, 2) + "..." + item.real_name.substr(-5, 5);
    },
    // 修改图片文字上传
    bindTxt(item) {
      if (item.real_name == "") {
        this.$Message.error("请填写内容");
      }
      fileUpdateApi(item.att_id, {
        real_name: item.real_name,
      })
        .then((res) => {
          this.editName(item);
          item.isEdit = false;
          this.$Message.success(res.msg);
        })
        .catch((error) => {
          this.$Message.error(error.msg);
        });
    },
    openUpload() {
      this.uploadVisible = true;
    },
    uploadSuccess() {
      this.uploadVisible = false;
      this.fileData.page = 1;
      this.getFileList();
    },
  },
};
</script>

<style scoped lang="stylus">
	.searchNo{
		margin-top -250px;
		text-align center;
	}
  .nameStyle {
    position: absolute;
    white-space: nowrap;
    z-index: 9;
    background: #eee;
    height: 20px;
    line-height: 20px;
    color: #555;
    border: 1px solid #ebebeb;
    padding: 0 5px;
    // left: 56px;
    // bottom: -18px;
  }

  .iconbianji1 {
    font-size: 13px;
  }

  /deep/.ivu-badge-count {
    margin-top: 18px !important;
    margin-right: 19px !important;
  }

  /deep/ivu-tree-title-selected:hover {
    color: unset;
    background-color: unset;
  }

  /deep/.ivu-tree-title {
    padding: 0;
    display: unset!important;
  }
	
	/deep/.ivu-tree-title .ivu-span>span{
	      padding 5px 30px 5px 0
	}

  /deep/.ivu-tree ul li {
    margin: 0;
  }

  /deep/.ivu-tree-arrow {
    width: 17px;
    color: #626262;
  }

  /deep/.ivu-span:hover {
    background: #F5F5F5;
    color: rgba(0, 0, 0, 0.4) !important;
  }

  /deep/.ivu-tree-arrow i {
    vertical-align: bottom;
  }

  .Nav /deep/.ivu-icon-ios-arrow-forward:before {
    content: '\F341' !important;
    font-size: 20px;
  }

  /deep/.ivu-btn-icon-only.ivu-btn-small {
    padding: unset !important;
  }

  .selectTreeClass {
    background: #d5e8fc;
  }
  /deep/ .ivu-tree-title{
      background-color: rgba(0,0,0,0) !important;
      transition: all 0s !important;
    }
  .treeBox {
    width: 100%;
    height: 100%;
  
    /deep/ .ivu-tree-title-selected{
      color: #2D8cF0 !important;
       background-color: rgba(0,0,0,0) !important;
    }
    /deep/ .ivu-tree-title-selected:hover {
      color: #2D8cF0 !important;
      background-color: #f3f3f3 !important;
    }

    /deep/ .ivu-btn-icon-only {
      width: 20px !important;
      height: 20px !important;
    }

    /deep/ .ivu-tree-title:hover {
      color: #2D8cF0 !important;
      background-color: none !important;
    }
  }

  .pictrueList_pic {
    position: relative;
    width: 100px;
    cursor: pointer;

    img {
      width: 100%;
      height: 100px;
    }

    p {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      height: 20px;
      text-align: center;
    }

    .number {
      height: 33px;
    }

    .number {
      position: absolute;
      right: 0;
      top: 0;
    }
  }

  .trees-coadd {
    width: 100%;
    border-radius: 4px;
    overflow: hidden;
    position: relative;

    .scollhide {
      overflow-x: hidden;
      overflow-y: scroll;
      padding: 10px 0 10px 0;
      box-sizing: border-box;

      .trees {
        width: 100%;
        height: 374px;
      }
    }

    .scollhide::-webkit-scrollbar {
      // display: none;
			width: 4px!important; /*对垂直流动条有效*/
    }
		/*定义滑块 内阴影+圆角*/
		::-webkit-scrollbar-thumb{
		    -webkit-box-shadow: inset 0 0 6px #999;
		}
  }

.treeSel >>>.ivu-select-dropdown-list {
  padding: 0 5px !important;
  box-sizing: border-box;
  width: 200px;
}

  .imagesNo {
    display: flex;
    justify-content: center;
    flex-direction: column;
    align-items: center;
    margin: 65px auto;

    .imagesNo_sp {
      font-size: 13px;
      color: #dbdbdb;
      line-height: 3;
    }
  }

  .Modal {
    width: 100%;
    height: 100%;
    background: #fff !important;
  }

  .Nav {
    width: 100%;
    border-right: 1px solid #eee;
  }

  .colLeft {
    padding-right: 0 !important;
    height: 100%;
  }

  .conter {
    width: 100%;
    height: 100%;
    margin-left: 0 !important;
  }

  .conter .bnt {
    width: 100%;
    padding: 0 13px 10px 8px;
    box-sizing: border-box;
  }

  .conter .pictrueList {
    padding-left: 6px;
    width: 100%;
    overflow-x: hidden;
    overflow-y: auto;
    height: 390px;
  }

  .conter .pictrueList img {
    width: 100%;
    border: 2px solid #fff;
  }

  .conter .pictrueList img.on {
    border: 2px solid #5FB878;
  }

  .conter .footer {
    padding: 20px;
  }

  .demo-badge {
    width: 42px;
    height: 42px;
    background: transparent;
    border-radius: 6px;
    display: inline-block;
  }
.bnt /deep/ .ivu-tree-children {
  padding: 5px 0;
}

.trees-coadd /deep/ .ivu-tree-children .ivu-tree-arrow {
  line-height: 26px;
}
</style>