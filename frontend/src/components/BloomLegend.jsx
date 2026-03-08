const BLOOM = [
  ["Remember", "Resource"],
  ["Understand", "Quiz"],
  ["Apply", "Assignment"],
  ["Analyze", "Forum"],
  ["Evaluate", "Reflection"],
  ["Create", "Wiki/Project"],
];

export default function BloomLegend() {
  return (
    <div className="card bloom-legend">
      <h3>Bloom-Guided Course Pattern</h3>
      <div className="legend-grid">
        {BLOOM.map(([level, tool]) => (
          <div key={level} className="legend-item">
            <strong>{level}</strong>
            <span>{tool}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
