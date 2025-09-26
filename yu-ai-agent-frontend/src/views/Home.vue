<template>
  <div class="home-container">
    <h1 class="home-title">AI智能体应用中心</h1>
    <div class="app-grid">
      <div class="app-card" @click="navigateToApp('/love-app')">
        <div class="app-icon">💕</div>
        <h2 class="app-title">AI恋爱大师</h2>
        <p class="app-description">
          专业的恋爱咨询AI助手，为您提供情感建议和恋爱指导，帮助您更好地处理感情问题。
        </p>
      </div>
      
      <div class="app-card" @click="navigateToApp('/manus-app')">
        <div class="app-icon">🤖</div>
        <h2 class="app-title">AI超级智能体</h2>
        <p class="app-description">
          强大的AI智能助手，具备多种工具和能力，可以帮您解决各种复杂问题和任务。
        </p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  methods: {
    navigateToApp(path) {
      this.$router.push(path)
    }
  }
}
</script>
