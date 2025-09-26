<template>
  <div class="chat-container">
    <div class="chat-header">
      <div>
        <h2>🤖 AI超级智能体</h2>
        <p>智能助手为您服务</p>
      </div>
      <button @click="goHome" class="btn btn-secondary">返回首页</button>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(message, index) in messages" :key="index" 
           :class="['message', message.type, { 'step-message': message.isStep }]">
        <div v-if="message.type === 'ai'" class="message-avatar">AI</div>
        <div :class="['message-content', { 'step-content': message.isStep }]">{{ message.content }}</div>
        <div v-if="message.type === 'user'" class="message-avatar">我</div>
      </div>
      
      <div v-if="isLoading" class="message ai">
        <div class="message-avatar">AI</div>
        <div class="message-content">
          <div class="loading"></div>
          AI正在思考中...
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <input 
        v-model="inputMessage" 
        @keyup.enter="sendMessage"
        placeholder="请输入您的问题..."
        :disabled="isLoading"
      />
      <button 
        @click="sendMessage" 
        :disabled="!inputMessage.trim() || isLoading"
      >
        发送
      </button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'ManusApp',
  setup() {
    const router = useRouter()
    const messages = ref([])
    const inputMessage = ref('')
    const isLoading = ref(false)
    const messagesContainer = ref(null)
    let eventSource = null
    let stepCounter = 0

    // 检测是否是新步骤的函数
    const detectNewStep = (text) => {
      const stepKeywords = [
        '步骤', 'Step', '正在', '执行', '思考', '分析', '调用', '工具', 
        '结果', '完成', '开始', '处理', '计算', '搜索', '查询', '生成',
        '推理', '判断', '评估', '总结', '结论', '建议', '方案', '计划'
      ]
      
      // 检查是否包含步骤关键词
      const hasStepKeyword = stepKeywords.some(keyword => text.includes(keyword))
      
      // 检查是否是数字开头的步骤
      const isNumberedStep = /^\d+[\.\)]\s/.test(text)
      
      // 检查是否是明显的步骤分隔符
      const hasStepSeparator = text.includes('---') || text.includes('===') || text.includes('***')
      
      return hasStepKeyword || isNumberedStep || hasStepSeparator
    }

    // 初始化聊天
    const initChat = () => {
      stepCounter = 0
      messages.value = [
        {
          type: 'ai',
          content: '您好！我是AI超级智能体，拥有多种工具和能力，可以帮您解决各种复杂问题。请告诉我您需要什么帮助！🤖'
        }
      ]
    }

    // 发送消息
    const sendMessage = async () => {
      if (!inputMessage.value.trim() || isLoading.value) return

      const userMessage = inputMessage.value.trim()
      inputMessage.value = ''
      
      // 重置步骤计数器
      stepCounter = 0
      
      // 添加用户消息
      messages.value.push({
        type: 'user',
        content: userMessage
      })

      isLoading.value = true
      scrollToBottom()

      try {
        // 使用SSE连接
        const url = `http://localhost:8123/api/ai/manus/chat?message=${encodeURIComponent(userMessage)}`
        
        if (eventSource) {
          eventSource.close()
        }

        eventSource = new EventSource(url)
        
        let aiResponse = ''
        
        eventSource.onmessage = (event) => {
          if (event.data) {
            const stepData = event.data.trim()
            
            if (stepData) {
              // 更智能的步骤检测
              const isNewStep = detectNewStep(stepData)
              
              if (isNewStep) {
                // 结束当前流式消息
                const lastMessage = messages.value[messages.value.length - 1]
                if (lastMessage && lastMessage.type === 'ai' && lastMessage.isStreaming) {
                  lastMessage.isStreaming = false
                }
                
                // 增加步骤计数器
                stepCounter++
                
                // 创建新的步骤消息
                messages.value.push({
                  type: 'ai',
                  content: `🔹 步骤 ${stepCounter}: ${stepData}`,
                  isStreaming: true,
                  isStep: true
                })
              } else {
                // 继续当前步骤的内容
                const lastMessage = messages.value[messages.value.length - 1]
                if (lastMessage && lastMessage.type === 'ai' && lastMessage.isStreaming) {
                  lastMessage.content += stepData
                } else {
                  // 如果没有流式消息，创建新的
                  stepCounter++
                  messages.value.push({
                    type: 'ai',
                    content: `🔹 步骤 ${stepCounter}: ${stepData}`,
                    isStreaming: true,
                    isStep: true
                  })
                }
              }
              scrollToBottom()
            }
          }
        }

        eventSource.onerror = (error) => {
          console.error('SSE连接错误:', error)
          isLoading.value = false
          if (eventSource) {
            eventSource.close()
            eventSource = null
          }
          
          // 添加错误消息
          const lastMessage = messages.value[messages.value.length - 1]
          if (lastMessage && lastMessage.isStreaming) {
            lastMessage.content = aiResponse || '抱歉，连接出现问题，请重试。'
            lastMessage.isStreaming = false
          } else {
            messages.value.push({
              type: 'ai',
              content: '抱歉，连接出现问题，请重试。'
            })
          }
        }

        eventSource.addEventListener('close', () => {
          isLoading.value = false
          const lastMessage = messages.value[messages.value.length - 1]
          if (lastMessage && lastMessage.isStreaming) {
            lastMessage.isStreaming = false
          }
          if (eventSource) {
            eventSource.close()
            eventSource = null
          }
        })

      } catch (error) {
        console.error('发送消息失败:', error)
        isLoading.value = false
        messages.value.push({
          type: 'ai',
          content: '抱歉，发送消息失败，请重试。'
        })
      }
    }

    // 滚动到底部
    const scrollToBottom = () => {
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      })
    }

    // 返回首页
    const goHome = () => {
      if (eventSource) {
        eventSource.close()
        eventSource = null
      }
      router.push('/')
    }

    onMounted(() => {
      initChat()
    })

    // 组件卸载时关闭SSE连接
    const cleanup = () => {
      if (eventSource) {
        eventSource.close()
        eventSource = null
      }
    }

    return {
      messages,
      inputMessage,
      isLoading,
      messagesContainer,
      sendMessage,
      goHome,
      cleanup
    }
  },
  beforeUnmount() {
    this.cleanup()
  }
}
</script>
