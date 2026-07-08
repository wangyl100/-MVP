import { message, Modal } from 'ant-design-vue'
import { h } from 'vue'

/**
 * 通用操作反馈 composable。
 * 统一处理「死按钮」：删除确认、新建/编辑弹窗、详情抽屉、导出/查询等消息提示。
 * 业务页面只需调用对应方法，无需重复实现交互逻辑。
 */
export function useAction() {
  /** 删除类操作：二次确认 + 成功提示 */
  function confirmDelete(label: string, onOk?: () => void) {
    Modal.confirm({
      title: `确认删除`,
      content: `确定要删除「${label}」吗？删除后不可恢复。`,
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => {
        onOk?.()
        message.success(`已删除「${label}」`)
      }
    })
  }

  /** 归档/关闭/停用类操作：二次确认 */
  function confirmAction(title: string, label: string, onOk?: () => void) {
    Modal.confirm({
      title,
      content: `确定要对「${label}」执行此操作吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        onOk?.()
        message.success(`操作成功`)
      }
    })
  }

  /** 新建类操作：弹出简单的表单弹窗（mock 提交） */
  function openCreate(title: string, fieldsHint?: string) {
    Modal.info({
      title,
      width: 560,
      content: h('div', { style: 'padding:8px 0;color:#7b8794;font-size:13px;line-height:1.8' }, [
        h('p', { style: 'margin:0 0 12px' }, fieldsHint || '请填写相关信息后提交。'),
        h('div', { style: 'background:#f8fafd;border:1px solid #e8edf5;border-radius:8px;padding:12px;color:#9ba7b4' }, [
          '表单区域（演示占位）：',
          h('br'),
          '· 名称、描述、类型等字段',
          h('br'),
          '· 配置参数',
          h('br'),
          '· 关联资源'
        ])
      ]),
      okText: '提交',
      cancelText: '取消',
      onOk: () => message.success(`${title}已提交`)
    })
  }

  /** 编辑类操作：弹出编辑弹窗（mock） */
  function openEdit(title: string, label: string) {
    Modal.info({
      title: `编辑 - ${title}`,
      width: 560,
      content: h('div', { style: 'padding:8px 0;color:#7b8794;font-size:13px;line-height:1.8' }, [
        h('p', { style: 'margin:0 0 8px' }, `正在编辑：${label}`),
        h('div', { style: 'background:#f8fafd;border:1px solid #e8edf5;border-radius:8px;padding:12px;color:#9ba7b4' }, [
          '编辑表单（演示占位）：修改字段后点击保存。'
        ])
      ]),
      okText: '保存',
      cancelText: '取消',
      onOk: () => message.success('修改已保存')
    })
  }

  /** 详情类操作：弹出详情抽屉（mock） */
  function openDetail(title: string, record: Record<string, any>) {
    Modal.info({
      title: `详情 - ${title}`,
      width: 640,
      content: h('div', { style: 'padding:8px 0' }, [
        Object.entries(record).map(([k, v]) =>
          h('div', {
            style: 'display:flex;padding:6px 0;border-bottom:1px dashed #e8edf5;font-size:13px'
          }, [
            h('span', { style: 'width:120px;color:#9ba7b4;flex-shrink:0' }, k),
            h('span', { style: 'color:#1f2a37;word-break:break-all' }, String(v))
          ])
        )
      ]),
      okText: '关闭',
      onOk: () => {}
    })
  }

  /** 简单的成功提示（查询/重置/导出/导入/测试等） */
  function notify(action: string, label?: string) {
    message.success(label ? `${action}「${label}」成功` : `${action}成功`)
  }

  /** 信息提示（开发中/演示提示） */
  function info(text: string) {
    message.info(text)
  }

  /** 测试连接类操作：模拟异步测试 */
  function testConnection(label?: string) {
    const hide = message.loading('测试中...', 0)
    setTimeout(() => {
      hide()
      message.success(label ? `「${label}」连接正常` : '连接正常')
    }, 800)
  }

  return {
    confirmDelete,
    confirmAction,
    openCreate,
    openEdit,
    openDetail,
    notify,
    info,
    testConnection
  }
}
