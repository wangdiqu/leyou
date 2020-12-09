<template>
  <div>
    <v-layout class="px-3 pd-2">
      <v-flex xs2>
        <v-btn color="primary">新增品牌</v-btn>
      </v-flex>
      <v-spacer/>
      <v-flex xs4>
        <v-text-field label="搜索" hide-details append-icon="search" v-model="key"></v-text-field>
      </v-flex>
    </v-layout>
    <v-data-table
      :headers="headers"
      :items="brands"
      :pagination.sync="pagination"
      :total-items="totalBrands"
      :loading="loading"
      class="elevation-1"
    >
      <template slot="items" slot-scope="props">
        <td class="text-xs-center">{{ props.item.id }}</td>
        <td class="text-xs-center">{{ props.item.name }}</td>
        <td class="text-xs-center">
          <img v-if="props.item.image" :src="props.item.image" width="130" height="40">
          <span v-else>无</span>
        </td>
        <td class="text-xs-center">{{ props.item.letter }}</td>
        <td class="text-xs-center">
          <v-btn flat icon color="info">
            <v-icon small>edit</v-icon>
          </v-btn>
          <v-btn flat icon color="error">
            <v-icon small>delete</v-icon>
          </v-btn>
        </td>
      </template>
    </v-data-table>
  </div>
</template>

<script>

  export default {
    name: "MyBrand",
    data() {
      return {
        key: '',
        totalBrands: 0,
        brands: [],
        loading: false,
        pagination: {},
        headers: [
          {text: '品牌id', align: 'center', sortable: true, value: 'name'},
          {text: '品牌名称', align: 'center', sortable: true, value: 'calories'},
          {text: '品牌图片地址', align: 'center', sortable: true, value: 'fat'},
          {text: '品牌的首字母', align: 'center', sortable: true, value: 'carbs'},
          {text: '操作', align: 'center', sortable: false},
        ],
      }
    },
    created() {
      this.loadBrands();
    },
    watch: {//监控
      key() {
        this.pagination.page = 1;
      },
      pagination: {
        deep: true,
        handler() {
          this.loadBrands();
        }
      }
    },
    methods: {
      loadBrands() {
        this.loading = true;
        this.$http.get("/item/brand/page", {
          params: {
            desc: this.pagination.descending, //是否降序
            page: this.pagination.page, //当前页
            rows: this.pagination.rowsPerPage, //每页大小
            sortBy: this.pagination.sortBy, //排序字段
            search: this.key, //搜索条件
          }
        }).then(response => {
          this.brands = response.data.items,
            this.totalBrands = response.data.total,
            this.loading = false
        });
      }
    },

  }
</script>

<style scoped>

</style>
