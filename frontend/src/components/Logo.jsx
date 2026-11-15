/** Logo AGR-05: chiếc lá trong vòng tròn. */
export default function Logo({ size = 32, nen = '#ffffff', la = '#2f6b3f' }) {
  return (
    <svg width={size} height={size} viewBox="0 0 32 32" aria-hidden="true">
      <circle cx="16" cy="16" r="16" fill={nen} />
      <path d="M9 22c0-8 5-13 14-13 0 9-5 14-13 14l-1-1z" fill={la} />
      <path d="M9.5 22.5 19 13" stroke={nen} strokeWidth="1.6" strokeLinecap="round" />
    </svg>
  );
}
