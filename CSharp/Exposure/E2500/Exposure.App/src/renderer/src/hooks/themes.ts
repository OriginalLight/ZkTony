import { computed } from 'vue'

export default function useThemes() {
  const theme = localStorage.getItem('theme') || 'light'

  const currentTheme = computed(() => {
    return theme
  })
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const changeTheme = (value: any) => {
    if (value === 'dark') {
      localStorage.setItem('theme', 'dark')
      document.body.setAttribute('arco-theme', 'dark')
    } else {
      localStorage.setItem('theme', 'light')
      document.body.removeAttribute('arco-theme')
    }
  }

  const initTheme = () => {
    if (theme === 'dark') {
      document.body.setAttribute('arco-theme', 'dark')
    } else {
      document.body.removeAttribute('arco-theme')
    }
  }
  return {
    currentTheme,
    changeTheme,
    initTheme
  }
}
