const $ = (s) => document.querySelector(s);
const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const data = (v) => (v ? new Date(v).toLocaleString('pt-BR') : '—');
const moeda = (v) => (v == null ? '—' : Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }));

let session = JSON.parse(sessionStorage.getItem('session') || 'null');
let tab = 'bicicletas';

async function api(path, method = 'GET', body) {
  const headers = { 'Content-Type': 'application/json' };
  if (session) headers.Authorization = 'Bearer ' + session.token;
  const res = await fetch('/api' + path, { method, headers, body: body ? JSON.stringify(body) : undefined });
  const json = res.status === 204 ? null : await res.json().catch(() => null);
  if (res.status === 401 && session) { logout(); throw new Error('Sessão expirada. Entre novamente.'); }
  if (!res.ok) throw new Error(json?.erros?.length ? json.erros.join('; ') : json?.mensagem || 'Erro ' + res.status);
  return json;
}

function aviso(texto, ok = false) {
  const el = $('#msg');
  el.textContent = texto || '';
  el.className = ok ? 'ok' : '';
}

/* ---------- telas ---------- */
const views = {
  async bicicletas() {
    const lista = await api('/bicicletas');
    const admin = session.role === 'ADMIN';
    const form = admin ? `
      <form id="f-bike" class="row">
        <input name="codigo" placeholder="Código" required>
        <input name="modelo" placeholder="Modelo" required>
        <button class="primary">Adicionar bicicleta</button>
      </form>` : '';
    const linhas = lista.map((b) => `
      <tr>
        <td>${esc(b.codigo)}</td><td>${esc(b.modelo)}</td>
        <td><span class="tag ${b.status}">${b.status}</span></td>
        ${admin ? `<td class="actions">${b.status === 'ALUGADA' ? '' : `
          <button class="ghost" data-act="bike-status" data-id="${b.id}" data-codigo="${esc(b.codigo)}" data-modelo="${esc(b.modelo)}" data-status="${b.status === 'MANUTENCAO' ? 'DISPONIVEL' : 'MANUTENCAO'}">${b.status === 'MANUTENCAO' ? 'Liberar' : 'Enviar p/ manutenção'}</button>
          <button class="ghost" data-act="bike-del" data-id="${b.id}">Excluir</button>`}</td>` : ''}
      </tr>`).join('');
    return form + tabela(['Código', 'Modelo', 'Status', ...(admin ? [''] : [])], linhas, 'Nenhuma bicicleta cadastrada.');
  },

  async clientes() {
    const lista = await api('/clientes');
    const linhas = lista.map((c) => `
      <tr><td>${esc(c.nome)}</td><td>${esc(c.cpf)}</td><td>${esc(c.telefone)}</td><td>${esc(c.email)}</td></tr>`).join('');
    return tabela(['Nome', 'CPF', 'Telefone', 'E-mail'], linhas, 'Nenhum cliente cadastrado.');
  },

  async locacoes() {
    const admin = session.role === 'ADMIN';
    const [locs, bikes, clientes] = await Promise.all([
      api('/locacoes'), api('/bicicletas'), admin ? api('/clientes') : Promise.resolve([]),
    ]);
    const livres = bikes.filter((b) => b.status === 'DISPONIVEL');
    const form = `
      <form id="f-loc" class="row">
        ${admin ? `<select name="clienteId" required><option value="">Cliente</option>${clientes.map((c) => `<option value="${c.id}">${esc(c.nome)}</option>`).join('')}</select>` : ''}
        <select name="bicicletaId" required><option value="">Bicicleta disponível</option>${livres.map((b) => `<option value="${b.id}">${esc(b.codigo)} · ${esc(b.modelo)}</option>`).join('')}</select>
        <button class="primary">Alugar</button>
      </form>`;
    const linhas = locs.slice().reverse().map((l) => `
      <tr>
        <td>${esc(l.clienteNome)}</td><td>${esc(l.bicicletaCodigo)}</td>
        <td>${data(l.dataRetirada)}</td><td>${data(l.dataDevolucao)}</td><td>${moeda(l.valor)}</td>
        <td><span class="tag ${l.status}">${l.status}</span></td>
        <td class="actions">${l.status === 'ATIVA' ? `<button class="ghost" data-act="devolver" data-id="${l.id}">Devolver</button>` : ''}</td>
      </tr>`).join('');
    return form + tabela(['Cliente', 'Bicicleta', 'Retirada', 'Devolução', 'Valor', 'Status', ''], linhas, 'Nenhuma locação registrada.');
  },
};

function tabela(cabecalho, linhas, vazio) {
  if (!linhas) return `<div class="table-wrap"><p class="empty">${vazio}</p></div>`;
  return `<div class="table-wrap"><table><thead><tr>${cabecalho.map((h) => `<th>${h}</th>`).join('')}</tr></thead><tbody>${linhas}</tbody></table></div>`;
}

async function mostrar(nome = tab) {
  tab = nome;
  const nomes = { bicicletas: 'Bicicletas', clientes: 'Clientes', locacoes: 'Locações' };
  const abas = session.role === 'ADMIN' ? ['bicicletas', 'clientes', 'locacoes'] : ['bicicletas', 'locacoes'];
  $('#tabs').innerHTML = abas.map((a) => `<button data-tab="${a}" aria-current="${a === tab}">${nomes[a]}</button>`).join('');
  try {
    $('#content').innerHTML = await views[tab]();
  } catch (e) {
    if (session) aviso(e.message);
  }
}

function entrar(s) {
  session = s;
  sessionStorage.setItem('session', JSON.stringify(s));
  $('#login').hidden = true;
  $('#app').hidden = false;
  $('#who').textContent = `${s.username} (${s.role})`;
  aviso('');
  mostrar('bicicletas');
}

function logout() {
  session = null;
  sessionStorage.removeItem('session');
  $('#app').hidden = true;
  $('#login').hidden = false;
}

/* ---------- eventos ---------- */
document.addEventListener('submit', async (e) => {
  e.preventDefault();
  const f = Object.fromEntries(new FormData(e.target));
  try {
    if (e.target.id === 'f-login') {
      $('#login-msg').textContent = '';
      entrar(await api('/auth/login', 'POST', f));
    } else if (e.target.id === 'f-bike') {
      await api('/bicicletas', 'POST', f);
      aviso('Bicicleta adicionada.', true);
      mostrar();
    } else if (e.target.id === 'f-loc') {
      await api('/locacoes', 'POST', { bicicletaId: Number(f.bicicletaId), clienteId: f.clienteId ? Number(f.clienteId) : null });
      aviso('Bicicleta alugada.', true);
      mostrar();
    }
  } catch (err) {
    if (e.target.id === 'f-login') $('#login-msg').textContent = err.message;
    else aviso(err.message);
  }
});

document.addEventListener('click', async (e) => {
  const el = e.target.closest('[data-tab], [data-act], #logout');
  if (!el) return;
  if (el.id === 'logout') return logout();
  if (el.dataset.tab) { aviso(''); return mostrar(el.dataset.tab); }
  const { act, id } = el.dataset;
  try {
    if (act === 'bike-del') {
      if (!confirm('Excluir esta bicicleta?')) return;
      await api('/bicicletas/' + id, 'DELETE');
      aviso('Bicicleta excluída.', true);
    } else if (act === 'bike-status') {
      await api('/bicicletas/' + id, 'PUT', { codigo: el.dataset.codigo, modelo: el.dataset.modelo, status: el.dataset.status });
      aviso('Status atualizado.', true);
    } else if (act === 'devolver') {
      const l = await api(`/locacoes/${id}/devolver`, 'PATCH');
      aviso(`Bicicleta devolvida. Valor: ${moeda(l.valor)}.`, true);
    }
    mostrar();
  } catch (err) {
    aviso(err.message);
  }
});

if (session) entrar(session);
