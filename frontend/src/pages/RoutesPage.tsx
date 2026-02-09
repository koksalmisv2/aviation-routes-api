import React, { useEffect, useState } from 'react';
import {
  Select,
  DatePicker,
  Button,
  Typography,
  message,
  Drawer,
  Empty,
  Spin,
  Card,
} from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import api from '../api/axiosInstance';
import type { LocationDTO, RouteDTO, TransportationSegmentDTO } from '../types';

const { Title, Text } = Typography;

/** Group routes by the flight segment's via-airport. */
interface RouteGroup {
  viaLabel: string;
  routes: RouteDTO[];
}

function groupRoutesByVia(routes: RouteDTO[]): RouteGroup[] {
  const groups: Record<string, RouteDTO[]> = {};

  for (const route of routes) {
    const flightSeg = route.segments.find((s) => s.segmentType === 'FLIGHT');
    const viaLabel = flightSeg
      ? `Via ${flightSeg.from.name} (${flightSeg.from.locationCode})`
      : 'Direct';

    if (!groups[viaLabel]) groups[viaLabel] = [];
    groups[viaLabel].push(route);
  }

  return Object.entries(groups).map(([viaLabel, routes]) => ({ viaLabel, routes }));
}

/** Get a summary label for a route, e.g. "BUS → FLIGHT → UBER" */
function routeSummary(route: RouteDTO): string {
  return route.segments.map((s) => s.type).join(' → ');
}

/** Map transport type to a display-friendly label */
function typeLabel(type: string): string {
  const map: Record<string, string> = {
    FLIGHT: 'Flight',
    BUS: 'Bus',
    SUBWAY: 'Subway',
    UBER: 'Uber',
  };
  return map[type] || type;
}

/** Color for the timeline dot */
function dotColor(segType: string): string {
  switch (segType) {
    case 'FLIGHT':
      return '#1677ff';
    case 'BEFORE_FLIGHT':
      return '#52c41a';
    case 'AFTER_FLIGHT':
      return '#fa8c16';
    default:
      return '#999';
  }
}

const RoutesPage: React.FC = () => {
  const [locations, setLocations] = useState<LocationDTO[]>([]);
  const [originId, setOriginId] = useState<number | undefined>();
  const [destinationId, setDestinationId] = useState<number | undefined>();
  const [date, setDate] = useState<string | undefined>();
  const [routes, setRoutes] = useState<RouteDTO[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [selectedRoute, setSelectedRoute] = useState<RouteDTO | null>(null);

  useEffect(() => {
    api
      .get<LocationDTO[]>('/routes/locations')
      .then(({ data }) => setLocations(data))
      .catch(() => message.error('Failed to load locations'));
  }, []);

  const handleSearch = async () => {
    if (!originId || !destinationId || !date) {
      message.warning('Please select origin, destination, and date');
      return;
    }
    setLoading(true);
    setRoutes(null);
    try {
      const { data } = await api.get<RouteDTO[]>('/routes', {
        params: { originId, destinationId, date },
      });
      setRoutes(data);
    } catch {
      message.error('Failed to search routes');
    } finally {
      setLoading(false);
    }
  };

  const openDetail = (route: RouteDTO) => {
    setSelectedRoute(route);
    setDrawerOpen(true);
  };

  const groups = routes ? groupRoutesByVia(routes) : [];

  /** Build ordered list of stops from segments */
  const buildStops = (segments: TransportationSegmentDTO[]) => {
    const stops: { location: LocationDTO; transportAfter?: string }[] = [];
    for (let i = 0; i < segments.length; i++) {
      const seg = segments[i];
      stops.push({ location: seg.from, transportAfter: seg.type });
    }
    // Add last destination
    const lastSeg = segments[segments.length - 1];
    stops.push({ location: lastSeg.to });
    return stops;
  };

  return (
    <>
      {/* Search bar */}
      <div
        style={{
          display: 'flex',
          gap: 12,
          flexWrap: 'wrap',
          alignItems: 'center',
          marginBottom: 24,
        }}
      >
        <div>
          <Text type="secondary" style={{ display: 'block', marginBottom: 4, fontSize: 12 }}>
            Origin
          </Text>
          <Select
            showSearch
            placeholder="Select origin"
            optionFilterProp="label"
            style={{ width: 220 }}
            value={originId}
            onChange={setOriginId}
            options={locations.map((l) => ({
              value: l.id!,
              label: `${l.name} (${l.locationCode})`,
            }))}
          />
        </div>
        <div>
          <Text type="secondary" style={{ display: 'block', marginBottom: 4, fontSize: 12 }}>
            Destination
          </Text>
          <Select
            showSearch
            placeholder="Select destination"
            optionFilterProp="label"
            style={{ width: 220 }}
            value={destinationId}
            onChange={setDestinationId}
            options={locations.map((l) => ({
              value: l.id!,
              label: `${l.name} (${l.locationCode})`,
            }))}
          />
        </div>
        <div>
          <Text type="secondary" style={{ display: 'block', marginBottom: 4, fontSize: 12 }}>
            Date
          </Text>
          <DatePicker
            style={{ width: 160 }}
            onChange={(d) => setDate(d ? d.format('YYYY-MM-DD') : undefined)}
          />
        </div>
        <div style={{ alignSelf: 'flex-end' }}>
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch} loading={loading}>
            Search
          </Button>
        </div>
      </div>

      {/* Results */}
      {loading && (
        <div style={{ textAlign: 'center', padding: 48 }}>
          <Spin size="large" />
        </div>
      )}

      {routes !== null && !loading && routes.length === 0 && (
        <Empty description="No routes found for the selected criteria" />
      )}

      {routes !== null && !loading && routes.length > 0 && (
        <div>
          <Title level={5} style={{ marginBottom: 16 }}>
            Available Routes
          </Title>
          {groups.map((group) => (
            <div key={group.viaLabel} style={{ marginBottom: 16 }}>
              <Text strong style={{ fontSize: 14, display: 'block', marginBottom: 8 }}>
                {group.viaLabel}
              </Text>
              {group.routes.map((route, idx) => (
                <Card
                  key={idx}
                  hoverable
                  size="small"
                  style={{ marginBottom: 8, cursor: 'pointer' }}
                  onClick={() => openDetail(route)}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    {route.segments.map((seg, sIdx) => (
                      <React.Fragment key={sIdx}>
                        <Text>{seg.from.name}</Text>
                        <Text
                          type="secondary"
                          style={{
                            fontSize: 12,
                            background: '#f0f0f0',
                            padding: '2px 8px',
                            borderRadius: 4,
                          }}
                        >
                          {typeLabel(seg.type)}
                        </Text>
                      </React.Fragment>
                    ))}
                    <Text>{route.segments[route.segments.length - 1].to.name}</Text>
                  </div>
                </Card>
              ))}
            </div>
          ))}
        </div>
      )}

      {/* Route Detail Drawer */}
      <Drawer
        title="Route Details"
        placement="right"
        width={380}
        onClose={() => setDrawerOpen(false)}
        open={drawerOpen}
        footer={
          <Button type="link" block onClick={() => setDrawerOpen(false)}>
            Close
          </Button>
        }
      >
        {selectedRoute && (
          <div>
            <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
              {routeSummary(selectedRoute)}
            </Text>

            {/* Vertical timeline */}
            <div style={{ paddingLeft: 12 }}>
              {buildStops(selectedRoute.segments).map((stop, idx, arr) => {
                const isLast = idx === arr.length - 1;
                const segType = idx < selectedRoute.segments.length
                  ? selectedRoute.segments[idx].segmentType
                  : undefined;
                return (
                  <div key={idx} style={{ display: 'flex', gap: 16, minHeight: isLast ? 'auto' : 64 }}>
                    {/* Timeline column */}
                    <div
                      style={{
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        width: 20,
                      }}
                    >
                      {/* Dot */}
                      <div
                        style={{
                          width: 14,
                          height: 14,
                          borderRadius: '50%',
                          border: `3px solid ${segType ? dotColor(segType) : '#999'}`,
                          background: '#fff',
                          flexShrink: 0,
                          marginTop: 4,
                        }}
                      />
                      {/* Line */}
                      {!isLast && (
                        <div
                          style={{
                            flex: 1,
                            width: 2,
                            borderLeft: '2px dashed #d9d9d9',
                            minHeight: 36,
                          }}
                        />
                      )}
                    </div>

                    {/* Content column */}
                    <div style={{ paddingTop: 0, paddingBottom: isLast ? 0 : 8 }}>
                      <Text strong style={{ display: 'block', lineHeight: '22px' }}>
                        {stop.location.name}
                        {stop.location.locationCode && (
                          <Text type="secondary"> ({stop.location.locationCode})</Text>
                        )}
                      </Text>
                      {stop.transportAfter && (
                        <Text
                          type="secondary"
                          style={{ fontSize: 13, display: 'block', marginTop: 6 }}
                        >
                          {typeLabel(stop.transportAfter)}
                        </Text>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </Drawer>
    </>
  );
};

export default RoutesPage;
