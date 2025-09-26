<template>
  <div class="chat-container">
    <div class="chat-header">
      <div>
        <h2>💕 AI恋爱大师</h2>
        <p>聊天室ID: {{ chatId }}</p>
      </div>
      <button @click="goHome" class="btn btn-secondary">返回首页</button>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(message, index) in messages" :key="index" 
           :class="['message', message.type]">
        <div v-if="message.type === 'ai'" class="message-avatar">AI</div>
        <div class="message-content" v-html="formatMessageContent(message.content)"></div>
        <div v-if="message.type === 'user'" class="message-avatar">我</div>
      </div>
      
      <div v-if="isLoading" class="message ai">
        <div class="message-avatar">AI</div>
        <div class="message-content thinking-bubble">
          <div class="thinking-dots">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
          {{ thinkingMessage }}
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <input 
        v-model="inputMessage" 
        @keyup.enter="sendMessage"
        placeholder="请输入您的问题..."
        :disabled="isLoading"
        type="text"
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
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'LoveApp',
  setup() {
    const router = useRouter()
    const messages = ref([])
    const inputMessage = ref('')
    const isLoading = ref(false)
    const chatId = ref('')
    const messagesContainer = ref(null)
    const thinkingMessage = ref('')
    let eventSource = null
    let thinkingInterval = null

    // 思考中的提示语数组
    const thinkingMessages = [
      '正在为你用心分析...',
      '让我想想如何帮你...',
      '嗯，你的情况我正在分析...',
      '给我一点时间，为你定制最佳建议...',
      '正在梳理思路，马上给你答案...',
      '💕 正在用心思考中...'
    ]

    // 生成聊天室ID
    const generateChatId = () => {
      return 'love_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    }

    // 初始化聊天室
    const initChat = () => {
      chatId.value = generateChatId()
      messages.value = [
        {
          type: 'ai',
          content: '您好！我是AI恋爱大师，专门为您提供恋爱咨询和情感指导。请告诉我您遇到的情感问题，我会尽力帮助您！💕'
        }
      ]
    }

    // 格式化消息内容
    const formatMessageContent = (content) => {
      if (!content) return ''
      
      try {
        // 对于所有消息，首先将换行符转换为<br>
        let formatted = content.replace(/\n/g, '<br>')
        
        // 检查是否是恋爱心理专家介绍文本的特殊格式
        if (content.includes('我是深耕恋爱心理领域的专家') && content.includes('🌸 如果你是')) {
          // 使用更简单的格式化方式，确保内容能正常显示
          formatted = '<div class="love-expert-intro">' + 
                     formatted.replace(/\*\*([^*]+)\*\*/g, '<span class="status-highlight">$1</span>') + 
                     '</div>'
        }
        
        return formatted
      } catch (error) {
        console.error('格式化消息失败:', error)
        // 如果格式化失败，返回原始内容并转换换行符
        return content.replace(/\n/g, '<br>')
      }
    }

    // 随机切换思考中的提示语
    const startThinkingAnimation = () => {
      thinkingMessage.value = thinkingMessages[0]
      thinkingInterval = setInterval(() => {
        const randomIndex = Math.floor(Math.random() * thinkingMessages.length)
        thinkingMessage.value = thinkingMessages[randomIndex]
      }, 2000) // 每2秒切换一次
    }

    // 停止思考中的动画
    const stopThinkingAnimation = () => {
      if (thinkingInterval) {
        clearInterval(thinkingInterval)
        thinkingInterval = null
      }
    }

    // 发送消息
    const sendMessage = async () => {
      if (!inputMessage.value.trim() || isLoading.value) return

      const userMessage = inputMessage.value.trim()
      inputMessage.value = ''
      
      // 添加用户消息
      messages.value.push({
        type: 'user',
        content: userMessage
      })

      isLoading.value = true
      startThinkingAnimation()
      scrollToBottom()

      try {
        // 使用SSE连接
        const url = `http://localhost:8123/api/ai/love_app/chat/sse?message=${encodeURIComponent(userMessage)}&chatId=${chatId.value}`
        
        if (eventSource) {
          eventSource.close()
        }

        eventSource = new EventSource(url)
        
        let aiResponse = ''
        
        eventSource.onmessage = (event) => {
          if (event.data) {
            aiResponse += event.data
            // 更新最后一条AI消息或创建新消息
            const lastMessage = messages.value[messages.value.length - 1]
            if (lastMessage && lastMessage.type === 'ai' && lastMessage.isStreaming) {
              lastMessage.content = aiResponse
            } else {
              messages.value.push({
                type: 'ai',
                content: aiResponse,
                isStreaming: true
              })
            }
            scrollToBottom()
          }
        }

        eventSource.onerror = (error) => {
          console.error('SSE连接错误:', error)
          isLoading.value = false
          stopThinkingAnimation()
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
          stopThinkingAnimation()
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
        stopThinkingAnimation()
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
      stopThinkingAnimation()
      if (eventSource) {
        eventSource.close()
        eventSource = null
      }
      router.push('/')
    }

    // 清理资源
    const cleanup = () => {
      stopThinkingAnimation()
      if (eventSource) {
        eventSource.close()
        eventSource = null
      }
    }

    onMounted(() => {
      initChat()
    })

    onBeforeUnmount(() => {
      cleanup()
    })

    return {
      messages,
      inputMessage,
      isLoading,
      chatId,
      messagesContainer,
      thinkingMessage,
      sendMessage,
      goHome,
      cleanup,
      formatMessageContent
    }
  }
}
</script>
