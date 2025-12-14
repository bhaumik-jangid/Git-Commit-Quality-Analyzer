const analyzeBtn = document.getElementById('analyzeBtn');
const repoPathEl = document.getElementById('repoPath');
const repoDisplay = document.getElementById('repoDisplay');
const statusEl = document.getElementById('status');

const totalEl = document.getElementById('total');
const goodPctEl = document.getElementById('goodPct');
const poorPctEl = document.getElementById('poorPct');
const avgEl = document.getElementById('avg');
const tbody = document.querySelector('#commitsTable tbody');

const repoDropdown = document.getElementById('repoDropdown');
const repoUrlInput = document.getElementById('repoUrl');

const repoPathText = document.getElementById('repoPathText');
const repoAuthor = document.getElementById('repoAuthor');
const repoCreated = document.getElementById('repoCreated');


let pieChart, lineChart;

/* ---------- PIE GLOW PLUGIN ---------- */
const pieGlowPlugin = {
    id: 'pieGlow',
    beforeDatasetDraw(chart) {
        if (chart.config.type !== 'pie') return;
        const ctx = chart.ctx;
        ctx.save();
        ctx.shadowColor = 'rgba(96,165,250,0.55)';
        ctx.shadowBlur = 30;
    },
    afterDatasetDraw(chart) {
        chart.ctx.restore();
    }
};

/* ---------- ANALYZE ---------- */
async function analyze() {
    const repoUrl = document.getElementById('repoUrl').value.trim();
    if (!repoUrl) {
        alert('Enter repository URL');
        return;
    }

    showLoader(true);

    try {
        const res = await fetch('/api/repo/prepare', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ repoUrl })
        });

        if (!res.ok) throw new Error('Repo prepare failed');

        const { repoPath } = await res.json();
        repoDisplay.textContent = repoPath;

        // now call existing analyzer
        const commitsRes = await fetch(
            `/api/analyze?repoPath=${encodeURIComponent(repoPath)}`
        );
        const commits = await commitsRes.json();
        render(commits);

    } catch (e) {
        alert(e.message);
    } finally {
        showLoader(false);
    }
}


/* ---------- RENDER ---------- */
function render(commits) {
    tbody.innerHTML = '';

    let good = 0, poor = 0, sum = 0;
    const scores = [];

    commits.forEach(c => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
      <td>${c.hash.slice(0, 7)}</td>
      <td>${c.author}</td>
      <td>${c.date}</td>
      <td>${c.message}</td>
      <td>${c.score}</td>
      <td>${c.category}</td>
    `;
        tbody.appendChild(tr);

        if (c.category === 'Good') good++;
        if (c.category === 'Poor') poor++;
        sum += c.score;
        scores.push(c.score);
    });

    const total = commits.length;
    totalEl.textContent = total;
    goodPctEl.textContent = total ? Math.round((good / total) * 100) + '%' : '0%';
    poorPctEl.textContent = total ? Math.round((poor / total) * 100) + '%' : '0%';
    avgEl.textContent = total ? (sum / total).toFixed(1) : '0';

    /* PIE */
    if (pieChart) pieChart.destroy();
    pieChart = new Chart(document.getElementById('pieChart'), {
        type: 'pie',
        plugins: [pieGlowPlugin],
        data: {
            labels: ['Good', 'Average', 'Poor'],
            datasets: [{
                data: [good, total - good - poor, poor],
                backgroundColor: [
                    'rgba(16,185,129,1)',
                    'rgba(245,158,11,1)',
                    'rgba(239,68,68,1)'
                ],
                hoverOffset: 6
            }]
        },
        options: {
            plugins: { legend: { display: false } },
            animation: { duration: 160 }
        }
    });

    /* LINE */
    if (lineChart) lineChart.destroy();
    lineChart = new Chart(document.getElementById('lineChart'), {
        type: 'line',
        data: {
            labels: scores.map((_, i) => i + 1),
            datasets: [{
                data: scores,
                borderColor: '#60a5fa',
                borderWidth: 2,
                pointRadius: 0,
                tension: 0.3
            }]
        },
        options: {
            plugins: { legend: { display: false } }
        }
    });
}

analyzeBtn.addEventListener('click', analyze);

// default value
repoPathEl.value = '/home/azazil/Desktop/dev-pro/git-commit-quality-analyzer';

function showLoader(show) {
  document.getElementById('loadingOverlay').style.display =
    show ? 'flex' : 'none';
}
