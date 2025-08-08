import { createStore } from 'vuex'
import user from './modules/user'
import reservoir from './modules/reservoir'
import waterQuality from './modules/waterQuality'

export default createStore({
  modules: {
    user,
    reservoir,
    waterQuality
  }
}) 