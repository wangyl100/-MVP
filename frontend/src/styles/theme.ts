import { theme as antdTheme } from 'ant-design-vue'
import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context'

/**
 * UI 风格单一颜色来源（与 global.less 中的 Less 变量保持同值）。
 * 对应《UI风格.md》§4.1 浅色基线 / §4.2 暗色基线 / §4.3 状态色 / §8.2 图表基线。
 */
export const palette = {
  // 品牌
  sidebarBlue: '#0050B3',
  primary: '#2F6BFF',
  primaryLight: '#4C84FF',
  primaryLightBg: '#EAF2FF',
  cyanAccent: '#53C7E8',
  // 表面
  pageBg: '#F5F7FB',
  cardBg: '#FFFFFF',
  cardBgSecondary: '#F8FAFD',
  borderWeak: '#E8EDF5',
  borderStrong: '#D7E0EE',
  // 文字
  textPrimary: '#1F2A37',
  textSecondary: '#7B8794',
  textTertiary: '#9BA7B4',
  // 状态
  statusNormal: '#33B26D',
  statusWarning: '#F08A5D',
  statusDanger: '#E96A5F',
  // 图表
  microTrend: '#6AC7BA',
  gridLineLight: '#E9E9E9',
  // 暗色
  pageBgDark: '#0F1726',
  cardBgDark: '#172133',
  cardBgSecondaryDark: '#121C2E',
  headerBgDark: '#101B2D',
  borderDark: '#233049',
  borderStrongDark: '#1E2A40',
  textPrimaryDark: '#E8EDF8',
  textSecondaryDark: '#A6B3C6',
  textTertiaryDark: '#7E8AA1'
} as const

// 数字与英文统一采用「钉钉进步体 / DingTalk Latin」风格，提升指标数字稳定观感（§5.1）
const fontFamily =
  "'PingFang SC','Microsoft YaHei','DingTalk Latin','Inter','Roboto',Arial,sans-serif"

function baseToken() {
  return {
    colorPrimary: palette.primary,
    colorSuccess: palette.statusNormal,
    colorWarning: palette.statusWarning,
    colorError: palette.statusDanger,
    colorInfo: palette.primary,
    colorLink: palette.primary,
    colorTextBase: palette.textPrimary,
    colorBgBase: '#FFFFFF',
    colorText: palette.textPrimary,
    colorTextSecondary: palette.textSecondary,
    colorTextTertiary: palette.textTertiary,
    colorTextDescription: palette.textTertiary,
    colorBorder: palette.borderWeak,
    colorBorderSecondary: palette.borderStrong,
    colorBgLayout: palette.pageBg,
    colorBgContainer: palette.cardBg,
    colorBgContainerSecondary: palette.cardBgSecondary,
    colorFill: palette.borderWeak,
    colorFillSecondary: palette.cardBgSecondary,
    colorFillTertiary: palette.pageBg,
    colorFillQuaternary: palette.pageBg,
    borderRadius: 10,
    borderRadiusLG: 12,
    borderRadiusSM: 8,
    fontSize: 14,
    fontSizeLG: 16,
    fontFamily,
    wireframe: false,
    controlHeight: 32
  }
}

function darkTokenOverrides() {
  return {
    colorBgBase: palette.pageBgDark,
    colorTextBase: palette.textPrimaryDark,
    colorText: palette.textPrimaryDark,
    colorTextSecondary: palette.textSecondaryDark,
    colorTextTertiary: palette.textTertiaryDark,
    colorTextDescription: palette.textTertiaryDark,
    colorBgLayout: palette.pageBgDark,
    colorBgContainer: palette.cardBgDark,
    colorBgContainerSecondary: palette.cardBgSecondaryDark,
    colorBorder: palette.borderDark,
    colorBorderSecondary: palette.borderStrongDark,
    colorFill: palette.borderDark,
    colorFillSecondary: palette.borderStrongDark,
    colorFillTertiary: palette.pageBgDark,
    colorFillQuaternary: palette.pageBgDark
  }
}

// 侧边栏菜单恒为 theme="dark"，因此始终用 dark* 系列 token 驱动（v4.2.6 OverrideToken['Menu']）。
// 浅色模式侧边栏为 #0050B3 实色，菜单项同色融合；暗色模式侧边栏为深蓝渐变，菜单项透明以透出渐变。
function menuComponent(isDark: boolean) {
  return {
    darkItemBg: isDark ? 'transparent' : palette.sidebarBlue,
    darkSubMenuItemBg: isDark ? 'transparent' : palette.sidebarBlue,
    darkItemColor: 'rgba(232,237,248,0.85)',
    darkItemHoverColor: '#FFFFFF',
    darkItemHoverBg: 'rgba(255,255,255,0.10)',
    darkItemSelectedColor: '#FFFFFF',
    darkItemSelectedBg: 'rgba(255,255,255,0.18)',
    itemHeight: 44,
    itemMarginInline: 8,
    itemBorderRadius: 8
  }
}

export function buildThemeConfig(isDark: boolean): ThemeConfig {
  return {
    algorithm: isDark ? antdTheme.darkAlgorithm : antdTheme.defaultAlgorithm,
    token: {
      ...baseToken(),
      ...(isDark ? darkTokenOverrides() : {})
    } as ThemeConfig['token'],
    components: {
      Menu: menuComponent(isDark) as any,
      Layout: {
        headerBg: isDark ? palette.headerBgDark : '#FFFFFF',
        bodyBg: isDark ? palette.pageBgDark : palette.pageBg,
        siderBg: isDark ? 'transparent' : palette.sidebarBlue
      },
      Card: {
        borderRadiusLG: 12,
        paddingLG: 20,
        colorBorderSecondary: isDark ? palette.borderDark : palette.borderWeak
      },
      Button: { borderRadius: 8, controlHeight: 32 },
      Tag: { borderRadiusSM: 4 },
      Table: {
        headerBg: isDark ? palette.cardBgSecondaryDark : palette.cardBgSecondary,
        headerColor: isDark ? palette.textSecondaryDark : palette.textSecondary,
        rowHoverBg: isDark ? palette.cardBgSecondaryDark : palette.cardBgSecondary
      }
    } as ThemeConfig['components'],
    hashed: true
  }
}
