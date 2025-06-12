/**
 * Enhanced UI components for Rule Engine GARNIT
 * Provides modern form validation, accessibility improvements, and user feedback
 */

class RuleEngineUI {
  constructor() {
    this.init();
  }

  init() {
    this.setupFormValidation();
    this.setupAccessibleModals();
    this.setupToastNotifications();
    this.setupFABButtons();
    this.setupKeyboardNavigation();
  }

  setupFormValidation() {
    const forms = document.querySelectorAll('form[data-validate="true"]');
    
    forms.forEach(form => {
      const validator = new ModernFormValidator(form);
      
      const requiredFields = form.querySelectorAll('[required]');
      requiredFields.forEach(field => {
        validator.addRule(field.name || field.id, 
          ModernFormValidator.validators.required, 
          `${this.getFieldLabel(field)} is required`);
      });
      
      const emailFields = form.querySelectorAll('input[type="email"]');
      emailFields.forEach(field => {
        validator.addRule(field.name || field.id,
          ModernFormValidator.validators.email,
          'Please enter a valid email address');
      });
      
      form.addEventListener('submit', (e) => {
        if (!validator.validateForm()) {
          e.preventDefault();
          this.showToast('Please correct the form errors before submitting', 'error');
        }
      });
    });
  }

  setupAccessibleModals() {
    const modalTriggers = document.querySelectorAll('[data-modal-target]');
    
    modalTriggers.forEach(trigger => {
      trigger.addEventListener('click', (e) => {
        e.preventDefault();
        const targetId = trigger.getAttribute('data-modal-target');
        const modal = document.getElementById(targetId);
        
        if (modal) {
          this.openModal(modal, trigger);
        }
      });
    });
    
    const modalCloseButtons = document.querySelectorAll('[data-modal-close]');
    modalCloseButtons.forEach(button => {
      button.addEventListener('click', (e) => {
        e.preventDefault();
        const modal = button.closest('.modal');
        if (modal) {
          this.closeModal(modal);
        }
      });
    });
  }

  openModal(modal, trigger) {
    modal.style.display = 'block';
    modal.setAttribute('aria-hidden', 'false');
    modal.setAttribute('role', 'dialog');
    modal.setAttribute('aria-modal', 'true');
    
    const backdrop = modal.querySelector('.modal-backdrop') || this.createBackdrop();
    modal.appendChild(backdrop);
    
    setTimeout(() => {
      backdrop.classList.add('show');
      modal.classList.add('show');
    }, 10);
    
    const firstFocusable = modal.querySelector('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
    if (firstFocusable) {
      firstFocusable.focus();
    }
    
    modal.setAttribute('data-previous-focus', trigger.id || '');
    
    document.addEventListener('keydown', this.handleModalKeydown.bind(this));
  }

  closeModal(modal) {
    const backdrop = modal.querySelector('.modal-backdrop');
    if (backdrop) {
      backdrop.classList.remove('show');
    }
    modal.classList.remove('show');
    
    setTimeout(() => {
      modal.style.display = 'none';
      modal.setAttribute('aria-hidden', 'true');
      
      const previousFocusId = modal.getAttribute('data-previous-focus');
      if (previousFocusId) {
        const previousElement = document.getElementById(previousFocusId);
        if (previousElement) {
          previousElement.focus();
        }
      }
    }, 300);
    
    document.removeEventListener('keydown', this.handleModalKeydown.bind(this));
  }

  createBackdrop() {
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop';
    backdrop.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.5);
      z-index: 999;
      opacity: 0;
      transition: opacity 0.3s ease;
    `;
    return backdrop;
  }

  handleModalKeydown(e) {
    if (e.key === 'Escape') {
      const openModal = document.querySelector('.modal.show');
      if (openModal) {
        this.closeModal(openModal);
      }
    }
  }

  setupToastNotifications() {
    if (!document.getElementById('toast-container')) {
      const container = document.createElement('div');
      container.id = 'toast-container';
      container.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 1050;
        max-width: 350px;
      `;
      document.body.appendChild(container);
    }
  }

  showToast(message, type = 'info', duration = 5000) {
    const container = document.getElementById('toast-container');
    if (!container) return;
    
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'polite');
    
    const colors = {
      success: '#d4edda',
      error: '#f8d7da',
      warning: '#fff3cd',
      info: '#d1ecf1'
    };
    
    const borderColors = {
      success: '#c3e6cb',
      error: '#f5c6cb',
      warning: '#ffeaa7',
      info: '#bee5eb'
    };
    
    toast.style.cssText = `
      background-color: ${colors[type] || colors.info};
      border: 1px solid ${borderColors[type] || borderColors.info};
      border-radius: 4px;
      padding: 12px 16px;
      margin-bottom: 10px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      opacity: 0;
      transform: translateX(100%);
      transition: all 0.3s ease;
      position: relative;
    `;
    
    const content = document.createElement('div');
    content.textContent = message;
    toast.appendChild(content);
    
    const closeButton = document.createElement('button');
    closeButton.innerHTML = '×';
    closeButton.style.cssText = `
      position: absolute;
      top: 8px;
      right: 12px;
      background: none;
      border: none;
      font-size: 18px;
      cursor: pointer;
      color: #666;
    `;
    closeButton.setAttribute('aria-label', 'Close notification');
    closeButton.onclick = () => this.hideToast(toast);
    toast.appendChild(closeButton);
    
    container.appendChild(toast);
    
    setTimeout(() => {
      toast.style.opacity = '1';
      toast.style.transform = 'translateX(0)';
    }, 10);
    
    if (duration > 0) {
      setTimeout(() => {
        this.hideToast(toast);
      }, duration);
    }
  }

  hideToast(toast) {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    
    setTimeout(() => {
      if (toast.parentNode) {
        toast.parentNode.removeChild(toast);
      }
    }, 300);
  }

  setupFABButtons() {
    const fabButtons = document.querySelectorAll('.fab-button');
    
    fabButtons.forEach(button => {
      button.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        width: 56px;
        height: 56px;
        border-radius: 50%;
        background-color: #007bff;
        color: white;
        border: none;
        box-shadow: 0 3px 5px -1px rgba(0,0,0,.2), 0 6px 10px 0 rgba(0,0,0,.14), 0 1px 18px 0 rgba(0,0,0,.12);
        cursor: pointer;
        transition: all 0.3s ease;
        z-index: 1000;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
      `;
      
      button.addEventListener('mouseenter', () => {
        button.style.backgroundColor = '#0056b3';
        button.style.transform = 'scale(1.05)';
      });
      
      button.addEventListener('mouseleave', () => {
        button.style.backgroundColor = '#007bff';
        button.style.transform = 'scale(1)';
      });
      
      button.addEventListener('focus', () => {
        button.style.outline = '2px solid #80bdff';
        button.style.outlineOffset = '2px';
      });
      
      button.addEventListener('blur', () => {
        button.style.outline = 'none';
      });
    });
  }

  setupKeyboardNavigation() {
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Tab') {
        document.body.classList.add('keyboard-navigation');
      }
    });
    
    document.addEventListener('mousedown', () => {
      document.body.classList.remove('keyboard-navigation');
    });
    
    const style = document.createElement('style');
    style.textContent = `
      .keyboard-navigation *:focus {
        outline: 2px solid #007bff !important;
        outline-offset: 2px !important;
      }
      
      .keyboard-navigation button:focus,
      .keyboard-navigation input:focus,
      .keyboard-navigation select:focus,
      .keyboard-navigation textarea:focus {
        box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25) !important;
      }
    `;
    document.head.appendChild(style);
  }

  getFieldLabel(field) {
    const label = document.querySelector(`label[for="${field.id}"]`);
    if (label) {
      return label.textContent.replace('*', '').trim();
    }
    
    return field.getAttribute('placeholder') || field.name || 'Field';
  }
}

document.addEventListener('DOMContentLoaded', () => {
  new RuleEngineUI();
});

window.RuleEngineUI = RuleEngineUI;
