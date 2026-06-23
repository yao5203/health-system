<template>
  <Transition name="splash">
    <div v-if="visible" class="splash-screen" :class="{ 'exit': isExiting }">
      <div class="splash-content">
        <div class="avatar-ring">
          <div class="avatar">
            <span v-if="!avatarUrl">{{ initials }}</span>
            <img v-else :src="avatarUrl" alt="avatar" />
          </div>
          <div class="ring"></div>
        </div>
        <h2 class="welcome-text">{{ welcomeText }}</h2>
        <p class="sub-text">{{ subText }}</p>
        <div class="loading-dots">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
      <div class="splash-bg">
        <div class="gradient-orb orb-1"></div>
        <div class="gradient-orb orb-2"></div>
        <div class="gradient-orb orb-3"></div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  visible: Boolean,
  isExiting: Boolean,
  userName: String,
  userType: {
    type: String,
    default: 'user'
  },
  avatarUrl: String
})

const initials = computed(() => {
  if (!props.userName) return '?'
  return props.userName.charAt(0).toUpperCase()
})

const welcomeText = computed(() => {
  const name = props.userName || '访客'
  return `欢迎回来，${name}`
})

const subText = computed(() => {
  if (props.userType === 'doctor') return '正在进入医生协作工作台...'
  if (props.userType === 'admin') return '正在进入系统管理员工作台...'
  return '正在进入您的健康中心...'
})
</script>

<style scoped>
.splash-screen {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  overflow: hidden;
}

.splash-screen.exit {
  opacity: 0;
  transform: scale(1.05);
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.splash-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: float 8s ease-in-out infinite;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #3b82f6, #06b6d4);
  bottom: -50px;
  left: -50px;
  animation-delay: -2s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #f59e0b, #f97316);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -4s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.95);
  }
}

.splash-content {
  position: relative;
  z-index: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.avatar-ring {
  position: relative;
  width: 120px;
  height: 120px;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: 600;
  color: white;
  box-shadow: 0 20px 40px rgba(99, 102, 241, 0.3);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ring {
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  border: 3px solid transparent;
  background: linear-gradient(135deg, #6366f1, #8b5cf6, #3b82f6) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  animation: pulse-ring 2s ease-in-out infinite;
}

@keyframes pulse-ring {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.7;
  }
}

.welcome-text {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #18181b;
  letter-spacing: -0.02em;
}

.sub-text {
  margin: 0;
  font-size: 15px;
  color: #71717a;
}

.loading-dots {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6366f1;
  animation: bounce 1.4s ease-in-out infinite both;
}

.loading-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.splash-enter-active,
.splash-leave-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.splash-enter-from,
.splash-leave-to {
  opacity: 0;
  transform: scale(1.1);
}
</style>
