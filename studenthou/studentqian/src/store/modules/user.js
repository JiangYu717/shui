import axios from 'axios'

// 从本地存储中获取初始状态
const getInitialState = () => {
  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    const userData = JSON.parse(savedUser)
    return {
      isLoggedIn: true,
      userId: userData.userId,
      username: userData.username,
      role: userData.role,
      token: userData.token
    }
  }
  return {
    isLoggedIn: false,
    userId: null,
    username: '',
    role: '',
    token: null
  }
}

const state = getInitialState()

const mutations = {
  SET_USER_DATA(state, data) {
    state.isLoggedIn = true
    state.userId = data.userId
    state.username = data.username
    state.role = data.role
    state.token = data.token || 'dummy-token-' + Date.now()  // 如果没有提供token，生成一个临时的
    // 保存到本地存储
    localStorage.setItem('user', JSON.stringify({
      userId: state.userId,
      username: state.username,
      role: state.role,
      token: state.token
    }))
  },
  CLEAR_USER_DATA(state) {
    state.isLoggedIn = false
    state.userId = null
    state.username = ''
    state.role = ''
    state.token = null
    // 清除本地存储
    localStorage.removeItem('user')
  }
}

const actions = {
  async login({ commit }, credentials) {
    try {
      const response = await axios.post('/api/users/login', credentials)
      const userData = response.data
      // 确保响应中包含所需的用户数据
      if (!userData.userId || !userData.username) {
        throw new Error('Invalid user data received')
      }
      commit('SET_USER_DATA', userData)
      return userData
    } catch (error) {
      throw error.response?.data || error.message
    }
  },
  
  async register(context, userData) {
    try {
      const response = await axios.post('/api/users/register', userData)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },
  
  logout({ commit }) {
    commit('CLEAR_USER_DATA')
  },

  // 初始化用户状态
  initializeStore({ commit }) {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      try {
        const userData = JSON.parse(savedUser)
        commit('SET_USER_DATA', userData)
      } catch (error) {
        commit('CLEAR_USER_DATA')
      }
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
} 