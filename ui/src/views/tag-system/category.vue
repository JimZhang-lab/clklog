<template>
  <div class="category-page public-block">
    <div class="category-layout">
      <section class="category-tree public-hoverItem">
        <div class="section-head">
          <div class="public-tableHead">标签分类</div>
          <el-button size="mini" type="primary" icon="el-icon-plus" @click="openAdd()">
            新建分类
          </el-button>
        </div>
        <el-tree
          :data="tree"
          node-key="id"
          default-expand-all
          highlight-current
          :expand-on-click-node="false"
          @node-click="selectNode"
        >
          <span slot-scope="{ data }" class="tree-node">
            <span>{{ data.displayName }}</span>
            <span class="tree-actions">
              <el-button type="text" icon="el-icon-plus" @click.stop="openAdd(data)" />
              <el-button type="text" icon="el-icon-edit" @click.stop="openEdit(data)" />
              <el-button type="text" icon="el-icon-delete" @click.stop="remove(data)" />
            </span>
          </span>
        </el-tree>
      </section>

      <section class="category-editor public-hoverItem">
        <div class="public-tableHead">{{ form.id ? "编辑分类" : "新建分类" }}</div>
        <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
          <el-form-item label="父级分类">
            <el-select v-model="form.parentId" clearable filterable placeholder="顶级分类">
              <el-option
                v-for="item in flatCategories"
                :key="item.id"
                :label="item.pathName"
                :value="item.id"
                :disabled="item.id === form.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="分类显示名" prop="displayName">
            <el-input v-model="form.displayName" placeholder="请输入分类显示名" />
          </el-form-item>
          <el-form-item label="顺序号">
            <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right" />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch
              v-model="form.status"
              active-value="enabled"
              inactive-value="disabled"
              active-text="启用"
              inactive-text="停用"
            />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="form.description" type="textarea" :rows="4" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script>
import {
  addCategoryApi,
  deleteCategoryApi,
  editCategoryApi,
  getCategoryTreeApi,
} from "@/api/sysmanage/userTag";

export default {
  name: "TagCategory",
  data() {
    return {
      tree: [],
      saving: false,
      form: this.emptyForm(),
      rules: {
        displayName: [{ required: true, message: "请输入分类显示名", trigger: "blur" }],
      },
    };
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName;
    },
    flatCategories() {
      const result = [];
      const walk = (nodes, prefix) => {
        (nodes || []).forEach((item) => {
          const pathName = prefix ? `${prefix} / ${item.displayName}` : item.displayName;
          result.push(Object.assign({}, item, { pathName }));
          walk(item.children, pathName);
        });
      };
      walk(this.tree, "");
      return result;
    },
  },
  watch: {
    projectName() {
      this.resetForm();
      this.loadTree();
    },
  },
  mounted() {
    this.loadTree();
  },
  methods: {
    emptyForm() {
      return {
        id: "",
        parentId: "",
        displayName: "",
        sortOrder: 0,
        status: "enabled",
        description: "",
      };
    },
    loadTree() {
      getCategoryTreeApi({ projectName: this.projectName }).then((res) => {
        this.tree = res.data || [];
      });
    },
    selectNode(data) {
      this.openEdit(data);
    },
    openAdd(parent) {
      this.form = this.emptyForm();
      this.form.parentId = parent ? parent.id : "";
    },
    openEdit(data) {
      this.form = Object.assign(this.emptyForm(), data);
      delete this.form.children;
    },
    resetForm() {
      this.form = this.emptyForm();
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate());
    },
    submit() {
      this.$refs.form.validate((valid) => {
        if (!valid) return;
        this.saving = true;
        const payload = Object.assign({ projectName: this.projectName }, this.form);
        const request = payload.id ? editCategoryApi(payload) : addCategoryApi(payload);
        request
          .then(() => {
            this.$message.success("保存成功");
            this.resetForm();
            this.loadTree();
          })
          .finally(() => {
            this.saving = false;
          });
      });
    },
    remove(data) {
      this.$confirm(`确认删除分类“${data.displayName}”吗？`, "提示", {
        type: "warning",
      }).then(() => {
        deleteCategoryApi({ id: data.id }).then(() => {
          this.$message.success("删除成功");
          if (this.form.id === data.id) this.resetForm();
          this.loadTree();
        });
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.category-page {
  .category-layout {
    display: grid;
    grid-template-columns: minmax(280px, 36%) minmax(420px, 1fr);
    gap: 16px;
  }

  .category-tree,
  .category-editor {
    min-height: 560px;
    padding: 20px;
    background: #fff;
    border-radius: 6px;
  }

  .section-head,
  .tree-node {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .section-head {
    margin-bottom: 18px;
  }

  .tree-node {
    width: 100%;
    padding-right: 8px;
  }

  .tree-actions {
    opacity: 0;
  }

  .tree-node:hover .tree-actions {
    opacity: 1;
  }

  .category-editor .el-select,
  .category-editor .el-input,
  .category-editor .el-textarea {
    width: min(100%, 520px);
  }

  @media (max-width: 900px) {
    .category-layout {
      grid-template-columns: 1fr;
    }
  }
}
</style>
