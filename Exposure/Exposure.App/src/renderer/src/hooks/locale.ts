import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

export default function useLocale() {
  const i18 = useI18n()
  const currentLocale = computed(() => {
    return i18.locale.value
  })
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const changeLocale = (value: any) => {
    if (i18.locale.value === value) {
      return
    }
    i18.locale.value = value
    localStorage.setItem('locale', value)
  }
  return {
    currentLocale,
    changeLocale
  }
}
