/**
 * Унифицированный показ ошибок на сайте.
 * Использование:
 *   showError('Ошибка: ...')
 *   showError('...', { containerId: 'auth-error' })
 */

function sanitizeErrorMessage(message) {
  if (!message) return 'Ошибка.';
  const s = String(message);

  // Если пришёл JSON вида {"error":"..."}
  try {
    const obj = JSON.parse(s);
    if (obj && typeof obj === 'object') {
      if (typeof obj.error === 'string' && obj.error.trim()) return obj.error.trim();
      if (typeof obj.message === 'string' && obj.message.trim()) return obj.message.trim();
    }
  } catch (e) {
    // ignore
  }

  // Убираем возможные длинные хвосты (например HTML/stack trace)
  // оставляем первую строку до переноса
  const firstLine = s.split(/\r?\n/)[0].trim();
  if (firstLine) return firstLine;
  return 'Ошибка.';
}

function showError(message, options = {}) {
  const text = sanitizeErrorMessage(message);

  const containerId = options.containerId || null;
  if (containerId) {
    const el = document.getElementById(containerId);
    if (el) {
      el.textContent = text;
      el.style.display = 'block';
      el.classList.add('app-error');
      if (options.hideAfterMs) {
        setTimeout(() => {
          el.style.display = 'none';
        }, options.hideAfterMs);
      }
      return;
    }
  }

  // Fallback: показываем рядом с формой (если передали контейнер через data атрибут)
  if (options.closestForm) {
    const root = options.closestForm;
    const target = root.querySelector('[data-error-container="true"]') || root.querySelector('.app-error');
    if (target) {
      target.textContent = text;
      target.style.display = 'block';
      target.classList.add('app-error');
      return;
    }
  }

  // Последний fallback — alert (но обычно стараемся не использовать)
  // eslint-disable-next-line no-alert
  alert(text);
}

async function getErrorTextFromResponse(response) {
  try {
    const contentType = response.headers.get('Content-Type') || '';
    if (contentType.includes('application/json')) {
      const json = await response.json();
      if (json && typeof json === 'object') {
        if (typeof json.error === 'string' && json.error.trim()) return json.error.trim();
        if (typeof json.message === 'string' && json.message.trim()) return json.message.trim();
      }
    }
  } catch (e) {
    // ignore
  }

  try {
    const text = await response.text();
    return sanitizeErrorMessage(text);
  } catch (e) {
    return 'Ошибка.';
  }
}

