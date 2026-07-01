import { useRef, useState, useCallback } from 'react';

interface AlphabetIndexBarProps {
  availableLetters: string[];
  selectedLetter: string | null;
  onLetterSelected: (letter: string) => void;
}

const ALL_LETTERS = [
  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
  'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '#',
];

export function AlphabetIndexBar({ availableLetters, selectedLetter, onLetterSelected }: AlphabetIndexBarProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [isDragging, setIsDragging] = useState(false);
  const availableSet = new Set(availableLetters);

  const getLetterAtPosition = useCallback(
    (clientY: number): string | null => {
      const container = containerRef.current;
      if (!container) return null;
      const rect = container.getBoundingClientRect();
      const y = clientY - rect.top;
      const itemHeight = rect.height / ALL_LETTERS.length;
      const index = Math.max(0, Math.min(ALL_LETTERS.length - 1, Math.round(y / itemHeight)));
      const letter = ALL_LETTERS[index];
      return availableSet.has(letter) ? letter : null;
    },
    [availableSet],
  );

  const handlePointerDown = useCallback(
    (e: React.PointerEvent) => {
      e.preventDefault();
      setIsDragging(true);
      const letter = getLetterAtPosition(e.clientY);
      if (letter) onLetterSelected(letter);
    },
    [getLetterAtPosition, onLetterSelected],
  );

  const handlePointerMove = useCallback(
    (e: React.PointerEvent) => {
      if (!isDragging) return;
      const letter = getLetterAtPosition(e.clientY);
      if (letter) onLetterSelected(letter);
    },
    [isDragging, getLetterAtPosition, onLetterSelected],
  );

  const handlePointerUp = useCallback(() => {
    setIsDragging(false);
  }, []);

  return (
    <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
      <div
        ref={containerRef}
        onPointerDown={handlePointerDown}
        onPointerMove={handlePointerMove}
        onPointerUp={handlePointerUp}
        onPointerLeave={handlePointerUp}
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'space-evenly',
          width: 24,
          height: '100%',
          minHeight: 280,
          cursor: 'pointer',
          userSelect: 'none',
          touchAction: 'none',
        }}
      >
        {ALL_LETTERS.map((letter) => {
          const isAvailable = availableSet.has(letter);
          const isSelected = selectedLetter === letter;
          const size = isSelected && isDragging ? 18 : 16;

          return (
            <div
              key={letter}
              onClick={() => {
                if (isAvailable) onLetterSelected(letter);
              }}
              style={{
                width: size,
                height: size,
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                backgroundColor: isSelected
                  ? 'rgba(var(--mdui-color-on-surface), 0.12)'
                  : isDragging && isAvailable
                    ? 'rgba(var(--mdui-color-on-surface), 0.06)'
                    : 'transparent',
                fontSize: isSelected ? 11 : 10,
                fontWeight: isSelected ? 700 : 400,
                color: isAvailable ? 'rgb(var(--mdui-color-on-surface))' : 'rgba(var(--mdui-color-on-surface), 0.3)',
                lineHeight: 1,
              }}
            >
              {letter}
            </div>
          );
        })}
      </div>

      {/* 拖动时显示的字母提示气泡 */}
      {isDragging && selectedLetter && (
        <div
          style={{
            position: 'absolute',
            right: 32,
            width: 40,
            height: 40,
            borderRadius: '50%',
            backgroundColor: 'rgb(var(--mdui-color-on-surface))',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 16,
            fontWeight: 700,
            color: 'rgb(var(--mdui-color-surface))',
            pointerEvents: 'none',
            transform: 'translateY(-50%)',
            top: '50%',
          }}
        >
          {selectedLetter}
        </div>
      )}
    </div>
  );
}