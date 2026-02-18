import { useEffect, useMemo, useState } from "react";
import LogsToolbar from "../ui/logs/LogsToolbar";
import LogsTable from "../ui/logs/LogsTable";
import LogDetailsCard from "../ui/logs/LogDetailsCard";

export default function LogsPage({
    onCreateMapping,
    onOpenMapping,
}: {
    onCreateMapping: (logItem: any) => void;
    onOpenMapping: (mappingId: string) => void;
}) {
    const [q, setQ] = useState("");
    const [data, setData] = useState<any | null>(null);
    const [selectedKey, setSelectedKey] = useState<string | null>(null);
    const [err, setErr] = useState<string | null>(null);

    function eventKey(it: any): string {
        const req = it?.request ?? {};
        const method = req?.method ?? "?";
        const url = req?.url ?? req?.absoluteUrl ?? "?";
        const t =
            it?.loggedDate ??
            it?.timestamp ??
            it?.request?.loggedDate ??
            it?.request?.timestamp ??
            it?.loggedDateString ??
            "";
        return it?.id ?? req?.id ?? `${method}|${url}|${String(t)}`;
    }

    function fmtTime(it: any) {
        const v =
            it?.loggedDate ??
            it?.timestamp ??
            it?.request?.loggedDate ??
            it?.request?.timestamp ??
            it?.loggedDateString;

        if (!v) return "";
        if (typeof v === "number") return new Date(v).toLocaleString();

        const s = String(v).trim();
        const asNum = Number(s);
        if (!Number.isNaN(asNum) && s === String(asNum)) {
            return new Date(asNum).toLocaleString();
        }
        return s;
    }

    async function loadList() {
        setErr(null);
        const r = await fetch("/api/requests");
        if (!r.ok) {
            setErr(`HTTP ${r.status}`);
            return;
        }
        const j = await r.json();
        setData(j);

        const arr: any[] = j?.requests ?? [];
        if (selectedKey) {
            const stillThere = arr.some((it: any) => eventKey(it) === selectedKey);
            if (!stillThere) setSelectedKey(null);
        }
    }

    useEffect(() => {
        loadList();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const items: any[] = useMemo(() => {
        const arr = data?.requests ?? [];
        if (!q.trim()) return arr;
        const needle = q.toLowerCase();
        return arr.filter((x: any) => JSON.stringify(x).toLowerCase().includes(needle));
    }, [data, q]);

    const selected = useMemo(() => {
        if (!selectedKey) return null;
        return (data?.requests ?? []).find((it: any) => eventKey(it) === selectedKey) ?? null;
    }, [data, selectedKey]);

    return (
        <div style={{ display: "grid", gap: 12 }}>
            <LogsToolbar query={q} onQueryChange={setQ} onRefresh={loadList} />

            {err && <div style={{ color: "crimson" }}>{err}</div>}

            <LogsTable
                items={items}
                selectedKey={selectedKey}
                onSelect={setSelectedKey}
                eventKey={eventKey}
                fmtTime={fmtTime}
            />

            <LogDetailsCard
                selected={selected}
                onCreateMapping={() => selected && onCreateMapping(selected)}
                onOpenMapping={onOpenMapping}
            />
        </div>
    );
}
