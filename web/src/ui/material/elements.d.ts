/**
 * React JSX 类型声明：@material/web 自定义元素（md-*）。
 * 覆盖 Phase 2+ 迁移中会用到的属性；未列出的 md-* 元素可在此追加。
 */
import React from 'react';

type Base = React.DetailedHTMLProps<React.HTMLAttributes<HTMLElement>, HTMLElement>;
/** 布尔属性——Web Components 只要属性存在即为 true，React 传 false 不能省略 */
type Bool = boolean | undefined;

declare global {
  namespace React {
    namespace JSX {
      interface IntrinsicElements {

        /* ── Icon ─────────────────────────────────────────────────── */
        'md-icon': Base;

        /* ── Buttons ──────────────────────────────────────────────── */
        'md-filled-button': { disabled?: Bool; href?: string; target?: string; 'trailing-icon'?: Bool; } & Base;
        'md-outlined-button': { disabled?: Bool; href?: string; target?: string; } & Base;
        'md-text-button': { disabled?: Bool; href?: string; target?: string; } & Base;
        'md-filled-tonal-button': { disabled?: Bool; href?: string; } & Base;
        'md-elevated-button': { disabled?: Bool; href?: string; } & Base;

        'md-icon-button': {
          disabled?: Bool;
          href?: string;
          target?: string;
          selected?: Bool;
          toggle?: Bool;
          variant?: 'standard' | 'filled' | 'filled-tonal' | 'outlined';
        } & Base;
        'md-filled-icon-button': { disabled?: Bool; toggle?: Bool; selected?: Bool; href?: string; } & Base;
        'md-filled-tonal-icon-button': { disabled?: Bool; toggle?: Bool; selected?: Bool; href?: string; } & Base;
        'md-outlined-icon-button': { disabled?: Bool; toggle?: Bool; selected?: Bool; href?: string; } & Base;

        /* ── FAB ──────────────────────────────────────────────────── */
        'md-fab': {
          label?: string;
          size?: 'small' | 'medium' | 'large';
          variant?: 'surface' | 'primary' | 'secondary' | 'tertiary';
          lowered?: Bool;
        } & Base;
        'md-branded-fab': { label?: string; lowered?: Bool; size?: 'medium' | 'large'; } & Base;

        /* ── Text fields ──────────────────────────────────────────── */
        'md-filled-text-field': {
          label?: string;
          value?: string;
          placeholder?: string;
          disabled?: Bool;
          readonly?: Bool;
          required?: Bool;
          type?: string;
          rows?: number;
          cols?: number;
          maxlength?: number;
          minlength?: number;
          'max-rows'?: number;
          error?: Bool;
          'error-text'?: string;
          'supporting-text'?: string;
          name?: string;
          autocomplete?: string;
          pattern?: string;
        } & Base;
        'md-outlined-text-field': {
          label?: string;
          value?: string;
          placeholder?: string;
          disabled?: Bool;
          readonly?: Bool;
          required?: Bool;
          type?: string;
          rows?: number;
          cols?: number;
          maxlength?: number;
          minlength?: number;
          'max-rows'?: number;
          error?: Bool;
          'error-text'?: string;
          'supporting-text'?: string;
          name?: string;
          autocomplete?: string;
          pattern?: string;
        } & Base;

        /* ── Selection controls ───────────────────────────────────── */
        'md-switch': {
          selected?: Bool;
          disabled?: Bool;
          icons?: Bool;
          'show-only-selected-icon'?: Bool;
          name?: string;
          value?: string;
          required?: Bool;
        } & Base;
        'md-checkbox': {
          checked?: Bool;
          disabled?: Bool;
          indeterminate?: Bool;
          name?: string;
          value?: string;
          required?: Bool;
        } & Base;
        'md-radio': {
          checked?: Bool;
          disabled?: Bool;
          name?: string;
          value?: string;
          required?: Bool;
        } & Base;

        /* ── Dialog ───────────────────────────────────────────────── */
        'md-dialog': {
          open?: Bool;
          quick?: Bool;
          'no-focus-trap'?: Bool;
          returnValue?: string;
        } & Base;

        /* ── List ─────────────────────────────────────────────────── */
        'md-list': Base;
        'md-list-item': {
          disabled?: Bool;
          selected?: Bool;
          active?: Bool;
          href?: string;
          target?: string;
          type?: 'text' | 'button' | 'link';
          'multi-line-support'?: Bool;
        } & Base;
        'md-item': Base;

        /* ── Progress ─────────────────────────────────────────────── */
        'md-circular-progress': { value?: number; indeterminate?: Bool; buffer?: number; } & Base;
        'md-linear-progress': { value?: number; indeterminate?: Bool; buffer?: number; } & Base;

        /* ── Navigation (labs) ────────────────────────────────────── */
        'md-navigation-bar': { 'active-index'?: number; 'hide-inactive-labels'?: Bool; } & Base;
        'md-navigation-tab': {
          label?: string;
          icon?: string;
          'active-icon'?: string;
          active?: Bool;
          'badge-value'?: string;
          'show-badge'?: Bool;
        } & Base;

        /* ── Cards (labs) ─────────────────────────────────────────── */
        'md-elevated-card': Base;
        'md-filled-card': Base;
        'md-outlined-card': Base;

        /* ── Misc ─────────────────────────────────────────────────── */
        'md-divider': { inset?: Bool; 'inset-start'?: Bool; 'inset-end'?: Bool; } & Base;
        'md-elevation': Base;
        'md-ripple': Base;
        'md-focus-ring': { 'for'?: string; inward?: Bool; } & Base;
        'md-badge': { value?: string; } & Base;

        /* ── Chips ────────────────────────────────────────────────── */
        'md-chip-set': Base;
        'md-assist-chip': { label?: string; disabled?: Bool; elevated?: Bool; href?: string; target?: string; } & Base;
        'md-filter-chip': { label?: string; disabled?: Bool; elevated?: Bool; selected?: Bool; removable?: Bool; href?: string; target?: string; } & Base;
        'md-input-chip': { label?: string; disabled?: Bool; avatar?: Bool; href?: string; target?: string; } & Base;
        'md-suggestion-chip': { label?: string; disabled?: Bool; elevated?: Bool; href?: string; target?: string; } & Base;

        /* ── Tabs ─────────────────────────────────────────────────── */
        'md-tabs': { activeTabIndex?: number; } & Base;
        'md-primary-tab': { disabled?: Bool; 'inline-icon'?: Bool; } & Base;
        'md-secondary-tab': { disabled?: Bool; } & Base;
      }
    }
  }
}

export {};
