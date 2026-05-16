const apiBase = '';

async function fetchJson(path){
  const res = await fetch(apiBase + path);
  if(!res.ok) throw new Error('Request failed');
  return await res.json();
}

async function postJson(path, body){
  const res = await fetch(apiBase + path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  if(!res.ok) throw new Error('Request failed');
  return await res.json();
}

function el(tag, cls){ const e=document.createElement(tag); if(cls) e.className=cls; return e; }

async function loadBooks(){
  const grid = document.getElementById('books-list');
  grid.innerHTML='';
  try{
    const books = await fetchJson('/api/books');
    if(books.length===0){ grid.textContent='No books yet.'; return; }
    for(const b of books){
      const card = el('div','card');
      const h = el('h3'); h.textContent=b.title; card.appendChild(h);
      const meta = el('div'); meta.innerHTML=`<span class="badge">${b.isbn}</span> · ${b.author}`; card.appendChild(meta);
      const avail = el('div'); avail.style.marginTop='8px'; avail.textContent=`Available ${b.copiesAvailable}/${b.copiesTotal}`; card.appendChild(avail);
      grid.appendChild(card);
    }
  }catch(e){ grid.textContent='Failed to load books.'; }
}

async function loadMembers(){
  const grid = document.getElementById('members-list');
  grid.innerHTML='';
  try{
    const members = await fetchJson('/api/members');
    if(members.length===0){ grid.textContent='No members yet.'; return; }
    for(const m of members){
      const card = el('div','card');
      const h = el('h3'); h.textContent=m.name; card.appendChild(h);
      const meta = el('div'); meta.innerHTML=`${m.email} · ${m.phone??''}`; card.appendChild(meta);
      grid.appendChild(card);
    }
  }catch(e){ grid.textContent='Failed to load members.'; }
}

function setupTabs(){
  const tabs = [
    {a:'tab-books', v:'view-books', on:()=>loadBooks()},
    {a:'tab-add-book', v:'view-add-book'},
    {a:'tab-members', v:'view-members', on:()=>loadMembers()},
    {a:'tab-add-member', v:'view-add-member'},
    {a:'tab-issue', v:'view-issue-loan'},
    {a:'tab-return', v:'view-return-loan'},
    {a:'tab-issued', v:'view-issued', on:()=>loadIssuedLoans()},
    {a:'tab-returned', v:'view-returned', on:()=>loadReturnedLoans()}
  ];
  function show(id){
    document.querySelectorAll('.view').forEach(x=>x.classList.remove('visible'));
    document.querySelector('#'+id).classList.add('visible');
    document.querySelectorAll('nav a').forEach(x=>x.classList.remove('active'));
  }
  for(const t of tabs){
    const a = document.getElementById(t.a);
    const view = document.getElementById(t.v);
    if(!a || !view) continue;
    a.addEventListener('click', e=>{
      e.preventDefault();
      show(t.v);
      a.classList.add('active');
      if(t.on) t.on();
    });
  }
}

function setupForms(){
  const addBook = document.getElementById('form-add-book');
  if(addBook){
    addBook.addEventListener('submit', async e=>{
      e.preventDefault();
      const body = {
        isbn: document.getElementById('isbn').value.trim(),
        title: document.getElementById('title').value.trim(),
        author: document.getElementById('author').value.trim(),
        copiesTotal: parseInt(document.getElementById('copiesTotal').value,10)
      };
      const out = document.getElementById('add-book-result');
      out.textContent = '...';
      try{ await postJson('/api/books', body); out.textContent = 'Book added.'; loadBooks(); }
      catch(err){ out.textContent = 'Failed: '+err.message; }
    });
  }
  const addMember = document.getElementById('form-add-member');
  if(addMember){
    addMember.addEventListener('submit', async e=>{
      e.preventDefault();
      const body = {
        name: document.getElementById('m-name').value.trim(),
        email: document.getElementById('m-email').value.trim(),
        phone: document.getElementById('m-phone').value.trim()
      };
      const out = document.getElementById('add-member-result');
      out.textContent = '...';
      try{ await postJson('/api/members', body); out.textContent = 'Member added.'; loadMembers(); }
      catch(err){ out.textContent = 'Failed: '+err.message; }
    });
  }
  const issueLoan = document.getElementById('form-issue-loan');
  if(issueLoan){
    issueLoan.addEventListener('submit', async e=>{
      e.preventDefault();
      const body = {
        bookId: parseInt(document.getElementById('loan-book-id').value,10),
        memberId: parseInt(document.getElementById('loan-member-id').value,10),
        days: parseInt(document.getElementById('loan-days').value,10)
      };
      const out = document.getElementById('issue-loan-result');
      out.textContent = '...';
      try{ const r = await postJson('/api/loans', body); out.textContent = 'Issued. Due '+r.dueDate; }
      catch(err){ out.textContent = 'Failed: '+err.message; }
    });
  }
  const returnLoan = document.getElementById('form-return-loan');
  if(returnLoan){
    returnLoan.addEventListener('submit', async e=>{
      e.preventDefault();
      const body = { loanId: parseInt(document.getElementById('return-loan-id').value,10) };
      const out = document.getElementById('return-loan-result');
      out.textContent = '...';
      try{ await postJson('/api/loans/return', body); out.textContent = 'Returned.'; }
      catch(err){ out.textContent = 'Failed: '+err.message; }
    });
  }
}

window.addEventListener('DOMContentLoaded', ()=>{ setupTabs(); setupForms(); loadBooks(); });

async function loadIssuedLoans(){
  const grid = document.getElementById('issued-list');
  if(!grid) return;
  grid.innerHTML='';
  try{
    const loans = await fetchJson('/api/loans');
    if(loans.length===0){ grid.textContent='No active loans.'; return; }
    for(const l of loans){
      const card = el('div','card');
      const h = el('h3'); h.textContent=`Loan #${l.id}`; card.appendChild(h);
      const meta = el('div');
      meta.innerHTML = `Book ID ${l.bookId} · Member ID ${l.memberId}`; card.appendChild(meta);
      const dates = el('div');
      dates.style.marginTop='8px';
      dates.textContent = `Issued ${l.loanDate} · Due ${l.dueDate??''}`;
      card.appendChild(dates);
      grid.appendChild(card);
    }
  }catch(e){ grid.textContent='Failed to load issued loans.'; }
}

async function loadReturnedLoans(){
  const grid = document.getElementById('returned-list');
  if(!grid) return;
  grid.innerHTML='';
  try{
    const loans = await fetchJson('/api/loans/returned');
    if(loans.length===0){ grid.textContent='No returned loans.'; return; }
    for(const l of loans){
      const card = el('div','card');
      const h = el('h3'); h.textContent=`Loan #${l.id}`; card.appendChild(h);
      const meta = el('div');
      meta.innerHTML = `Book ID ${l.bookId} · Member ID ${l.memberId}`; card.appendChild(meta);
      const dates = el('div');
      dates.style.marginTop='8px';
      dates.textContent = `Issued ${l.loanDate} · Returned ${l.returnDate}`;
      card.appendChild(dates);
      grid.appendChild(card);
    }
  }catch(e){ grid.textContent='Failed to load returned loans.'; }
}


