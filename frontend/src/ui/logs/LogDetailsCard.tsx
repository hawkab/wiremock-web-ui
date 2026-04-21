// Author: olshansky (c) 2026

export default function LogDetailsCard({
    selected,
    onCreateMapping,
    onOpenMapping,
}: {
    selected: any | null;
    onCreateMapping: () => void;
    onOpenMapping: (mappingId: string) => void;
}) {
    const mappingId: string | null =
        selected?.stubMapping?.id && typeof selected.stubMapping.id === "string" ? selected.stubMapping.id : null;
    const mappingName: string | null =
        selected?.stubMapping?.name && typeof selected.stubMapping.name === "string" ? selected.stubMapping.name : null;

    return (
        <div className="card">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 8 }}>
                <div style={{ fontWeight: 700 }}>Карточка</div>
                <button
                    onClick={onCreateMapping}
                    disabled={!selected}
                    title={!selected ? "Сначала выбери лог" : "Создать маппинг на основе этого запроса"}
                >
                    Создать маппинг из запроса
                </button>
            </div>

            {selected ? (
                <>
                    {selected?.wasMatched === true && mappingId && (
                        <div style={{ marginTop: 10, padding: 10, border: "1px solid #222", borderRadius: 8 }}>
                            <div style={{ fontWeight: 700, marginBottom: 6 }}>Маппинг найден</div>
                            <button onClick={() => onOpenMapping(mappingId)} className="mono">
                                Открыть маппинг: {mappingName ?? mappingId}
                            </button>
                        </div>
                    )}

                    <pre className="preWrap">{JSON.stringify(selected, null, 2)}</pre>
                </>
            ) : (
                <div style={{ marginTop: 8, opacity: 0.7 }}>Выбери запись из таблицы выше</div>
            )}
        </div>
    );
}
