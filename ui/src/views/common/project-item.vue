<template>
  <div :class="['project-item', isMove ? 'move' : '']" v-if="isMoveMode">
    <div
      :class="{
        'state-bar': true,
        [project.status.toLowerCase()]: true,
      }"
    ></div>
    <div class="content">
      <div class="content-top">
        <span class="concurrent" v-if="concurrent">可并发</span>
        <router-link
          :to="{
          name: 'workflow-execution-record-detail',
          query: { projectId: project.id },
        }"
        >
          <jm-text-viewer :value="project.name" class="title"/>
        </router-link>
      </div>
      <div class="time">
        <div class="running" v-if="project.status === ProjectStatusEnum.RUNNING">
          <span>执行时长：</span>
          <jm-timer :start-time="project.startTime"/>
        </div>
        <div class="running" v-else-if="project.status === ProjectStatusEnum.SUSPENDED">
          <span>挂起时长：</span>
          <jm-timer :start-time="project.suspendedTime"/>
        </div>
        <span v-else>最后完成时间：{{ datetimeFormatter(project.latestTime) }}</span>
      </div>
      <div class="time">
        下次执行时间：{{ datetimeFormatter(project.nextTime) }}
      </div>
      <div class="operation">
        <div class="top"></div>
        <div class="bottom"></div>
      </div>
    </div>
    <div class="cover"></div>
  </div>
  <div class="project-item" v-else>
    <div
      :class="{
        'state-bar': true,
        [project.status.toLowerCase()]: true,
      }"
    ></div>
    <div class="content">
      <div class="content-top">
        <span class="concurrent" v-if="concurrent">可并发</span>
        <router-link
          :to="{
          name: 'workflow-execution-record-detail',
          query: { projectId: project.id },
        }"
        >
          <jm-text-viewer :value="project.name" :class="{title:true,disabled:!enabled}"/>
        </router-link>
      </div>
      <div :class="{
        time: true,
        disabled: !enabled,
      }">
        <div class="running" v-if="project.status === ProjectStatusEnum.RUNNING">
          <span>执行时长：</span>
          <jm-timer :start-time="project.startTime"/>
        </div>
        <div class="running" v-else-if="project.status === ProjectStatusEnum.SUSPENDED">
          <span>挂起时长：</span>
          <jm-timer :start-time="project.suspendedTime"/>
        </div>
        <span v-else>最后完成时间：{{ datetimeFormatter(project.latestTime) }}</span>
      </div>
      <div :class="{
        time: true,
        disabled: !enabled,
      }">
        下次执行时间：{{ datetimeFormatter(project.nextTime) }}
      </div>
      <div class="operation">
        <jm-tooltip :content="`${enabled ? '' : '已禁用，不可'}触发`" placement="bottom">
          <button
            :class="{ execute: true, doing: !enabled || executing }"
            @click="execute(project.id)"
            @keypress.enter.prevent
          ></button>
        </jm-tooltip>
        <jm-tooltip
          v-if="project.triggerType === TriggerTypeEnum.WEBHOOK"
          content="Webhook"
          placement="bottom"
        >
          <button class="webhook" @click="webhookDrawerFlag = true"></button>
        </jm-tooltip>
        <jm-tooltip
          v-if="project.source === DslSourceEnum.LOCAL"
          content="编辑"
          placement="bottom"
        >
          <button class="edit" @click="edit(project.id)"></button>
        </jm-tooltip>
        <jm-tooltip v-else content="同步DSL" placement="bottom">
          <button
            :class="{ sync: true, doing: synchronizing }"
            @click="sync(project.id)"
            @keypress.enter.prevent
          ></button>
        </jm-tooltip>
        <jm-tooltip
          v-if="project.source === DslSourceEnum.GIT"
          content="打开git仓库"
          placement="bottom"
        >
          <button class="git-label" @click="openGit(project.gitRepoId)"></button>
        </jm-tooltip>
        <jm-tooltip
          v-if="project.dslType === DslTypeEnum.WORKFLOW"
          content="预览流程"
          placement="bottom"
        >
          <button class="workflow-label" @click="dslDialogFlag = true"></button>
        </jm-tooltip>
        <jm-tooltip
          v-else-if="project.dslType === DslTypeEnum.PIPELINE"
          content="预览管道"
          placement="bottom"
        >
          <button class="pipeline-label" @click="dslDialogFlag = true"></button>
        </jm-tooltip>
        <div class="more">
          <jm-dropdown trigger="click" placement="bottom-start">
            <span class="el-dropdown-link">
              <button class="btn-group"></button>
            </span>
            <template #dropdown>
              <jm-dropdown-menu>
                <jm-dropdown-item :disabled="abling" @click="able(project.id)">
                  <a
                    href="javascript: void(0)"
                    :class="enabled ? 'jm-icon-button-disable' : 'jm-icon-button-off'"
                    style="width: 90px; display: inline-block;"
                  >{{ enabled ? '禁用' : '启用' }}</a>
                </jm-dropdown-item>
                <jm-dropdown-item :disabled="deleting" @click="del(project.id)">
                  <a
                    href="javascript: void(0)"
                    class="jm-icon-button-delete"
                    style="width: 90px; display: inline-block;"
                  >删除</a>
                </jm-dropdown-item>
              </jm-dropdown-menu>
            </template>
          </jm-dropdown>
        </div>
      </div>
    </div>
    <webhook-drawer
      :current-project-id="project.id"
      :current-project-name="project.name"
      v-model:webhookVisible="webhookDrawerFlag"
    ></webhook-drawer>
    <project-preview-dialog
      v-if="dslDialogFlag"
      :project-id="project.id"
      @close="dslDialogFlag = false"
    />
    <div class="cover"></div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, PropType, ref, SetupContext } from 'vue';
import { DslSourceEnum, DslTypeEnum, ProjectStatusEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import { IProjectVo } from '@/api/dto/project';
import { active, del, executeImmediately, synchronize } from '@/api/project';
import { datetimeFormatter } from '@/utils/formatter';
import ProjectPreviewDialog from './project-preview-dialog.vue';
import WebhookDrawer from './webhook-drawer.vue';
import { useRouter } from 'vue-router';

export default defineComponent({
  components: { ProjectPreviewDialog, WebhookDrawer },
  props: {
    project: {
      type: Object as PropType<IProjectVo>,
      required: true,
    },
    // 控制item是否加上hover样式，根据对比id值判断
    move: {
      type: Boolean,
      default: false,
    },
    // 控制是否处于拖拽模式
    moveMode: {
      type: Boolean,
      default: false,
    },
    // 控制项目是否展示可并发
    concurrent: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['running', 'synchronized', 'deleted'],
  setup(props: any, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const isMove = computed<boolean>(() => props.move);
    const isMoveMode = computed<boolean>(() => props.moveMode);
    const executing = ref<boolean>(false);
    const abling = ref<boolean>(false);
    const synchronizing = ref<boolean>(false);
    const deleting = ref<boolean>(false);
    const enabled = ref<boolean>(props.project.enabled);
    const dslDialogFlag = ref<boolean>(false);
    const webhookDrawerFlag = ref<boolean>(false);
    return {
      isMoveMode,
      isMove,
      DslSourceEnum,
      DslTypeEnum,
      ProjectStatusEnum,
      TriggerTypeEnum,
      datetimeFormatter,
      executing,
      abling,
      synchronizing,
      deleting,
      enabled,
      dslDialogFlag,
      webhookDrawerFlag,
      execute: (id: string) => {
        if (!enabled.value || executing.value) {
          return;
        }

        const { triggerType } = props.project;
        const isWarning = triggerType === TriggerTypeEnum.WEBHOOK;

        let msg = '<div>确定要触发吗?</div>';
        if (isWarning) {
          msg +=
            '<div style="color: red; margin-top: 5px; font-size: 12px; line-height: normal;">注意：项目已配置webhook，手动触发可能会导致不可预知的结果，请慎重操作。</div>';
        }

        proxy
          .$confirm(msg, '触发项目执行', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: isWarning ? 'warning' : 'info',
            dangerouslyUseHTMLString: true,
          })
          .then(() => {
            executing.value = true;

            executeImmediately(id)
              .then(() => {
                proxy.$success('操作成功');
                executing.value = false;

                emit('running', id);
              })
              .catch((err: Error) => {
                proxy.$throw(err, proxy);
                executing.value = false;
              });
          })
          .catch(() => {
          });
      },
      able: (id: string) => {
        if (abling.value) {
          return;
        }

        const str = enabled.value ? '禁用' : '启用';
        const msg = props.project.mutable ? `
          <div>
            <span>确定要${str}吗?</span>
            <a href="https://v2.jianmu.dev/guide/global.html" target="_blank" class="jm-icon-button-help"></a>
          </div>
        ` : `
          <div>
            <span>${enabled.value ? '已启用' : '已禁用'}，不可修改</span>
            <a href="https://v2.jianmu.dev/guide/global.html" target="_blank" class="jm-icon-button-help"></a>
          </div>
          <div style="color: red; margin-top: 5px; font-size: 12px; line-height: normal;">若要修改，请通过DSL更新</div>
        `;

        proxy.$confirm(msg, `${str}项目`, {
          showConfirmButton: props.project.mutable,
          confirmButtonText: '确定',
          cancelButtonText: props.project.mutable ? '取消' : '关闭',
          type: 'info',
          dangerouslyUseHTMLString: true,
        }).then(async () => {
          abling.value = true;
          try {
            await active(id, !enabled.value);
            enabled.value = !enabled.value;

            proxy.$success(enabled.value ? '已启用' : '已禁用');
          } catch (err) {
            proxy.$throw(err, proxy);
          } finally {
            abling.value = false;
          }
        }).catch(() => {
        });
      },
      edit: (id: string) => {
        router.push({ name: 'update-project', params: { id } });
      },
      sync: (id: string) => {
        if (synchronizing.value) {
          return;
        }

        proxy
          .$confirm('确定要同步吗?', '同步DSL', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info',
          })
          .then(() => {
            synchronizing.value = true;

            synchronize(id)
              .then(() => {
                proxy.$success('同步成功');
                synchronizing.value = false;

                emit('synchronized', id);
              })
              .catch((err: Error) => {
                proxy.$throw(err, proxy);
                synchronizing.value = false;
              });
          })
          .catch(() => {
          });
      },
      del: (id: string) => {
        if (deleting.value) {
          return;
        }

        const { name } = props.project;

        let msg = '<div>确定要删除项目吗?</div>';
        msg += `<div style="margin-top: 5px; font-size: 12px; line-height: normal;">名称：${name}</div>`;

        proxy
          .$confirm(msg, '删除项目', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
            dangerouslyUseHTMLString: true,
          })
          .then(() => {
            deleting.value = true;

            del(id)
              .then(() => {
                proxy.$success('删除成功');
                deleting.value = false;

                emit('deleted', id);
              })
              .catch((err: Error) => {
                proxy.$throw(err, proxy);
                deleting.value = false;
              });
          })
          .catch(() => {
          });
      },
      openGit: (gitRepoId: string) => {
        window.open(`/view/repo/${gitRepoId}`);
      },
    };
  },
});
</script>

<style scoped lang="less">
@keyframes workflow-running {
  0% {
    background-position-x: -53.5px;
  }
  100% {
    background-position-x: 0;
  }
}

@-webkit-keyframes workflow-running {
  0% {
    background-position-x: -53.5px;
  }
  100% {
    background-position-x: 0;
  }
}

.project-item {
  margin: 0.8%;
  margin-bottom: 0px;
  width: 19.2%;
  min-width: 260px;
  background-color: #ffffff;
  box-shadow: 0px 0px 8px 4px #eff4f9;
  min-height: 166px;

  &.move {
    position: relative;
    cursor: move;

    .cover {
      display: block;
      position: absolute;
      box-sizing: border-box;
      width: 100%;
      height: 100%;
      border: 2px solid #096dd9;
      background-color: rgba(140, 140, 140, 0.3);
      top: 0;
      left: 0;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        right: 0;
        display: inline-block;
        width: 30px;
        height: 30px;
        background-image: url('@/assets/svgs/sort/drag.svg');
        background-repeat: no-repeat;
      }
    }
  }

  .cover {
    display: none;
  }

  &:hover {
    box-shadow: 0px 6px 16px 4px #e6eef6;
  }

  .state-bar {
    height: 8px;
    overflow: hidden;

    &.init {
      background-color: #979797;
    }

    &.running {
      background-image: repeating-linear-gradient(115deg,
      #10c2c2 0px,
      #58d4d4 1px,
      #58d4d4 10px,
      #10c2c2 11px,
      #10c2c2 16px);
      background-size: 106px 114px;
      animation: 3s linear 0s infinite normal none running workflow-running;
    }

    &.succeeded {
      background-color: #3ebb03;
    }

    &.failed {
      background-color: #cf1524;
    }

    &.suspended {
      background-color: #7986cb;
    }
  }

  .content {
    min-height: 116px;
    position: relative;
    padding: 20px 20px 16px 20px;

    .content-top {
      display: flex;
      align-items: center;

      a {
        flex: 1;
      }

      .concurrent {
        height: 20px;
        line-height: 20px;
        background: #FFF7E6;
        border-radius: 2px;
        padding: 3px;
        font-size: 12px;
        font-weight: 400;
        color: #6D4C41;
        margin-right: 5px;
      }
    }

    .title {
      margin-right: 20px;
      font-size: 16px;
      color: #082340;

      &.disabled {
        color: #979797;
      }

      &:hover {
        color: #096dd9;
      }
    }

    .time {
      margin-top: 10px;
      font-size: 13px;
      color: #6b7b8d;
      white-space: nowrap;

      .running {
        display: flex;

        .jm-timer {
          flex: 1
        }
      }

      &.disabled {
        color: #979797;
      }
    }

    .operation {
      margin-top: 18px;
      min-height: 26px;
      display: flex;
      align-items: center;

      button + button {
        margin-left: 18px;
      }

      button {
        width: 26px;
        height: 26px;
        background-color: transparent;
        border: 0;
        background-position: center center;
        background-repeat: no-repeat;
        cursor: pointer;
        outline: none;

        &:active {
          background-color: #eff7ff;
          border-radius: 4px;
        }

        &.execute {
          background-image: url('@/assets/svgs/btn/execute.svg');
        }

        &.edit {
          background-image: url('@/assets/svgs/btn/edit.svg');
        }

        &.sync {
          background-image: url('@/assets/svgs/btn/sync.svg');

          &.doing {
            animation: rotating 2s linear infinite;
          }
        }

        &.webhook {
          background-image: url('@/assets/svgs/btn/hook.svg');
        }

        &.git-label {
          background-image: url('@/assets/svgs/index/git-label.svg');
        }

        &.workflow-label {
          background-image: url('@/assets/svgs/index/workflow-label.svg');
        }

        &.pipeline-label {
          background-image: url('@/assets/svgs/index/pipeline-label.svg');
        }

        &.btn-group {
          background-image: url('@/assets/svgs/btn/more2.svg');
          position: absolute;
          right: 0px;
          top: 0px;
        }

        &.doing {
          opacity: 0.5;
          cursor: not-allowed;

          &:active {
            background-color: transparent;
          }
        }
      }

      .more {
        position: absolute;
        right: 3px;
        top: 5px;
        opacity: 0.65;

        &:hover {
          opacity: 1;
        }

        .el-dropdown-link {
          display: inline-block;
          width: 26px;
          height: 10px;
        }
      }
    }
  }
}

.project-item {
  margin-left: 0px;
}
</style>
