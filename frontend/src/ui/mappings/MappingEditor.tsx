// Author: olshansky (c) 2026

export default function MappingEditor({
    value,
    onChange,
    mappingName,
    onMappingNameChange,
    onSave,
    draftSourceLabel,
}: {
    value: string;
    onChange: (v: string) => void;
    mappingName: string;
    onMappingNameChange: (v: string) => void;
    onSave: () => void;
    draftSourceLabel: string | null;
}) {
    return (
        <>
            <div style={{ display: "grid", gap: 8, marginBottom: 8 }}>
                <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                    <button onClick={onSave}>Сохранить</button>
                    {draftSourceLabel && (
                        <span style={{ opacity: 0.7, fontSize: 12 }}>черновик из: {draftSourceLabel}</span>
                    )}
                </div>

                <input
                    value={mappingName}
                    onChange={(e) => onMappingNameChange(e.target.value)}
                    placeholder="Имя маппинга"
                    style={{ width: "100%" }}
                />
            </div>

            <textarea
                value={value}
                onChange={(e) => onChange(e.target.value)}
                placeholder="Выбери маппинг слева или создай из лога"
                style={{
                    width: "100%",
                    minHeight: 520,
                    borderRadius: 8,
                    border: "1px solid #333",
                    padding: 10,
                }}
                className="mono"
            />
        </>
    );
}
