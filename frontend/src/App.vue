<template>
  <el-container class="app-shell">
    <el-aside class="side-nav" width="220px">
      <div class="brand">
        <div class="brand-mark">站</div>
        <div>
          <div class="brand-title">驿站包裹管理</div>
          <div class="brand-subtitle">Parcel Station</div>
        </div>
      </div>

      <el-menu :default-active="$route.path" router class="nav-menu">
        <el-menu-item index="/parcels/pickup">
          <span>查询取件</span>
        </el-menu-item>
        <el-menu-item index="/parcels/inbound">
          <span>包裹入库</span>
        </el-menu-item>
        <el-menu-item index="/parcels">
          <span>包裹列表</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="page-header">
        <div>
          <h1>{{ $route.meta.title }}</h1>
        </div>
      </el-header>

      <el-main class="page-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style>
:root {
  color: #1f2937;
  background: #f5f7fb;
  font-family:
    Inter, "PingFang SC", "Microsoft YaHei", system-ui, -apple-system,
    BlinkMacSystemFont, "Segoe UI", sans-serif;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
}

.app-shell {
  min-height: 100vh;
  background: #f5f7fb;
}

.side-nav {
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 72px;
  padding: 0 20px;
  border-bottom: 1px solid #edf0f5;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 8px;
  background: #1f2937;
  color: #ffffff;
  font-weight: 700;
}

.brand-title {
  font-size: 16px;
  font-weight: 700;
}

.brand-subtitle {
  margin-top: 2px;
  color: #8a94a6;
  font-size: 12px;
}

.nav-menu {
  border-right: 0;
  padding: 12px;
}

.page-header {
  display: flex;
  align-items: center;
  height: 72px;
  padding: 0 28px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.page-header h1 {
  margin: 0;
  font-size: 20px;
  line-height: 1.3;
}

.page-main {
  padding: 24px 28px 36px;
}

.content-panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 22px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
}

.toolbar .el-input,
.toolbar .el-select {
  width: 220px;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.overdue-row {
  --el-table-tr-bg-color: #fff1f2;
}

@media (max-width: 760px) {
  .app-shell {
    display: block;
  }

  .side-nav {
    width: 100% !important;
    border-right: 0;
    border-bottom: 1px solid #e5e7eb;
  }

  .brand {
    height: 64px;
  }

  .nav-menu {
    display: flex;
    overflow-x: auto;
    padding: 0 8px 8px;
  }

  .nav-menu .el-menu-item {
    flex: 0 0 auto;
  }

  .page-header {
    height: auto;
    padding: 16px 18px;
  }

  .page-main {
    padding: 16px;
  }

  .toolbar .el-input,
  .toolbar .el-select {
    width: 100%;
  }
}
</style>
