/**
 * 节点类型枚举
 */
export enum NodeTypeEnum {
  CRON = 'cron',
  WEBHOOK = 'webhook',
  // START = 'start',
  // END = 'end',
  // CONDITION = 'condition',
  SHELL = 'shell',
  ASYNC_TASK = 'async-task',
}

/**
 * 缩放类型
 */
export enum ZoomTypeEnum {
  IN = 'IN',
  OUT = 'OUT',
  ORIGINAL = 'ORIGINAL',
  FIT = 'FIT',
}