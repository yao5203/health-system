<template>
  <section class="landing-grid">
    <div class="hero-copy">
      <h2>人体健康评测与康养系统</h2>
    </div>

    <div class="entry-cards">
      <RouterLink class="entry-card" to="/user/auth">
        <h3>进入用户端</h3>
        <span class="entry-cta">开始健康评测 →</span>
      </RouterLink>

      <RouterLink class="entry-card" to="/doctor/auth">
        <h3>进入医生端</h3>
        <span class="entry-cta">进入医生工作台 →</span>
      </RouterLink>
    </div>
  </section>
</template>

<script setup>
import { RouterLink } from 'vue-router'
</script>
