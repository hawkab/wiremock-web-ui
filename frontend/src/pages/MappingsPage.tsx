import { useEffect, useMemo, useState } from "react";
import type { MappingDraft } from "../App";
import MappingsList from "../ui/mappings/MappingsList";
import MappingEditor from "../ui/mappings/MappingEditor";

export default function MappingsPage({
                                         draft,
                                         clearDraft,
                                         openMappingId,
                                         clearOpenMappingId,
                                     }: {
    draft: MappingDraft;
    clearDraft: () => void;
    openMappingId: string | null;
    clearOpenMappingId: () => void;
}) {
    const [data, setData] = useState<any | null>(null);
    const [selectedId, setSelectedId] = useState<string | null>(null);
    const [editor, setEditor] = useState<string>("");
    const [mappingName, setMappingName] = useState<string>("");
    const [err, setErr] = useState<string | null>(null);

    async function load() {
        setErr(null);
        const r = await fetch("/api/mappings");
        if (!r.ok) {
            setErr(`HTTP ${r.status}`);
            return;
        }
        setData(await r.json());
    }

    useEffect(() => {
        load();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const mappings: any[] = useMemo(() => data?.mappings ?? [], [data]);

    function select(m: any) {
        setSelectedId(m.id);
        setEditor(JSON.stringify(m, null, 2));
        setMappingName(m.name ?? "");
        clearDraft();
    }

    useEffect(() => {
        if (draft?.json) {
            setSelectedId(null);
            setEditor(draft.json);
            try {
                const obj = JSON.parse(draft.json);
                setMappingName(obj?.name ?? "");
            } catch {
                setMappingName("");
            }
        }
    }, [draft?.json]);

    useEffect(() => {
        if (!openMappingId) return;
        const m = mappings.find((x: any) => x?.id === openMappingId);
        if (!m) return;
        select(m);
        clearOpenMappingId();
    }, [openMappingId, mappings]);

    async function saveOneButton() {
        setErr(null);

        let obj: any;
        try {
            obj = JSON.parse(editor || "{}");
        } catch {
            setErr("JSON не парсится");
            return;
        }

        if (mappingName?.trim()) obj.name = mappingName.trim();
        else delete obj.name;

        let resp: Response;
        if (selectedId) {
            resp = await fetch(`/api/mappings/${selectedId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(obj),
            });
            if (!resp.ok) {
                setErr(`Update failed: HTTP ${resp.status}`);
                return;
            }
        } else {
            resp = await fetch(`/api/mappings`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(obj),
            });
            if (!resp.ok) {
                setErr(`Create failed: HTTP ${resp.status}`);
                return;
            }
        }

        const save = await fetch("/api/mappings/save", { method: "POST" });
        if (!save.ok) {
            setErr(`Save-to-disk failed: HTTP ${save.status}`);
            return;
        }

        await load();
    }

    return (
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
            <div>
                <div style={{ display: "flex", gap: 8, marginBottom: 8 }}>
                    <button onClick={load}>Обновить</button>
                </div>

                {err && <div style={{ color: "crimson" }}>{err}</div>}

                <MappingsList mappings={mappings} selectedId={selectedId} onSelect={select} />
            </div>

            <div>
                <MappingEditor
                    value={editor}
                    onChange={setEditor}
                    mappingName={mappingName}
                    onMappingNameChange={setMappingName}
                    onSave={saveOneButton}
                    draftSourceLabel={draft?.sourceLabel ?? null}
                />
            </div>
        </div>
    );
}
