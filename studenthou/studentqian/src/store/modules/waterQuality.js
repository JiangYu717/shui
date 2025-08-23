import axios from 'axios'

const state = {
  waterQualityData: [],
  currentData: null,
  loading: false,
  error: null
}

const mutations = {
  SET_WATER_QUALITY_DATA(state, data) {
    state.waterQualityData = data
  },
  SET_CURRENT_DATA(state, data) {
    state.currentData = data
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_ERROR(state, error) {
    state.error = error
  },
  ADD_WATER_QUALITY_DATA(state, data) {
    state.waterQualityData.push(data)
  },
  UPDATE_WATER_QUALITY_DATA(state, updatedData) {
    const index = state.waterQualityData.findIndex(d => d.dataId === updatedData.dataId)
    if (index !== -1) {
      state.waterQualityData.splice(index, 1, updatedData)
    }
  },
  DELETE_WATER_QUALITY_DATA(state, dataId) {
    state.waterQualityData = state.waterQualityData.filter(d => d.dataId !== dataId)
  }
}

const actions = {
  async fetchAllWaterQualityData({ commit }) {
    commit('SET_LOADING', true)
    try {
      const response = await axios.get('/api/water-quality')
      commit('SET_WATER_QUALITY_DATA', response.data)
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  async fetchWaterQualityDataById({ commit }, dataId) {
    commit('SET_LOADING', true)
    try {
      const response = await axios.get(`/api/water-quality/${dataId}`)
      commit('SET_CURRENT_DATA', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  async fetchWaterQualityDataByReservoir({ commit }, reservoirId) {
    commit('SET_LOADING', true)
    try {
      const response = await axios.get(`/api/water-quality/reservoir/${reservoirId}`)
      commit('SET_WATER_QUALITY_DATA', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  async fetchWaterQualityDataByDateRange({ commit }, { startDate, endDate }) {
    commit('SET_LOADING', true)
    try {
      const response = await axios.get('/api/water-quality/date-range', {
        params: { startDate, endDate }
      })
      commit('SET_WATER_QUALITY_DATA', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  async fetchWaterQualityDataByRiskLevel({ commit }, riskLevel) {
    commit('SET_LOADING', true)
    try {
      const response = await axios.get(`/api/water-quality/risk-level/${riskLevel}`)
      commit('SET_WATER_QUALITY_DATA', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  async createWaterQualityData({ commit }, data) {
    try {
      const response = await axios.post('/api/water-quality', data)
      commit('ADD_WATER_QUALITY_DATA', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    }
  },
  
  async updateWaterQualityData({ commit }, { dataId, data }) {
    try {
      const response = await axios.put(`/api/water-quality/${dataId}`, data)
      commit('UPDATE_WATER_QUALITY_DATA', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    }
  },
  
  async deleteWaterQualityData({ commit }, dataId) {
    try {
      await axios.delete(`/api/water-quality/${dataId}`)
      commit('DELETE_WATER_QUALITY_DATA', dataId)
    } catch (error) {
      commit('SET_ERROR', error.response.data)
      throw error
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
} 